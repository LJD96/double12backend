package com.ljd.flink;

import com.alibaba.druid.pool.DruidDataSource;

import com.ljd.flink.vo.UserAction;
import com.ljd.utils.HttpClientUtils;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Liu JianDong
 * @create 2023-02-27-15:31
 **/
public class ReadFromKafka {
    private static final String TOPIC = "user_action_topic1";
    private static final int BROWSE = 1;
    private static final int COLLECT = 2;
    private static final int CART = 3;
    private static final int BUY = 4;
    private static final long WINDOW_SIZE = 2000;
    private static final long GLOBAL_OFFSET = 1000;
    private static Long pageView = 0L;
    private static Long all = 0L;
    private static final int NUM_PER_LINE = 5;
    private static Set<Integer> uniqueView = new HashSet<>();
    private static final Set<Integer> USER_ID = new HashSet<>();
    private static final Set<Integer> ITEM_ID = new HashSet<>();
    private static final Set<Integer> ITEM_CATEGORY_ID = new HashSet<>();
    private static final Map<Integer, Integer> ITEM_BROWSE_NUM = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> ITEM_COLLECT_NUM = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> ITEM_CART_NUM = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> ITEM_BUY_NUM = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> TOP10_BROWSE_ITEM = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> TOP10_BUY_ITEM = new ConcurrentHashMap<>();
    private static DruidDataSource dataSource;

    static{
        //1.硬编码初始化Druid连接池
        try {
            dataSource = new DruidDataSource();
            //四个基本属性
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://localhost:3306/test?serverTimezone=GMT%2B8&useSSL=false");
            dataSource.setUsername("root");
            dataSource.setPassword("LJD17863137669");
            //其他属性
            //初始大小
            dataSource.setInitialSize(3);
            //最大大小
            dataSource.setMaxActive(50);
            //最小大小
            dataSource.setMinIdle(3);
            //检查时间
            dataSource.setMaxWait(5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        env.setParallelism(2);
        Properties pro = new Properties();
        pro.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop1:9092,hadoop2:9092,hadoop3:9092");
        pro.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
        pro.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        FlinkKafkaConsumer<String> fkc = new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), pro);
        DataStreamSource<String> text = env.addSource(fkc);

        SingleOutputStreamOperator<UserAction> soso = text.flatMap(new FlatMapFunction<String, UserAction>() {
            @Override
            public void flatMap(String value, Collector<UserAction> out) throws Exception {
                String[] arr = value.split(",");
                if (arr.length == NUM_PER_LINE) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date time = sdf.parse(arr[4]);
                    UserAction userAction = new UserAction(
                            Integer.parseInt(arr[0]),
                            Integer.parseInt(arr[1]),
                            Integer.parseInt(arr[2]),
                            Integer.parseInt(arr[3]),
                            time
                    );
                    out.collect(userAction);
                }
            }
        });

        //-------------------------------------------------------------------------------------------------------------------------
        // 处理所有的值，不论key
        AllWindowedStream<UserAction, TimeWindow> userActionTimeWindowAllWindowedStream = soso.windowAll(new WindowAssigner<UserAction, TimeWindow>() {

            @Override
            public Collection<TimeWindow> assignWindows(UserAction element, long timestamp, WindowAssignerContext context) {
                // 获取当前元素的处理时间
                final long now = context.getCurrentProcessingTime();
                long size = WINDOW_SIZE;
                long start = now - (now - GLOBAL_OFFSET + size) % size;
                // 落在这个时间范围内的元素都会被分配到这个窗口中
                return Collections.singletonList(new TimeWindow(start, start + size));
            }

            @Override
            public Trigger<UserAction, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment env) {
                return new Trigger<UserAction, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(UserAction element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {
                        ctx.registerProcessingTimeTimer(window.maxTimestamp());
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        return TriggerResult.FIRE;
                    }

                    @Override
                    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
                        ctx.deleteProcessingTimeTimer(window.maxTimestamp());
                    }
                };
            }

            @Override
            public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
                return new TimeWindow.Serializer();
            }

            @Override
            public boolean isEventTime() {
                return false;
            }
        });

        SingleOutputStreamOperator<Object> process = userActionTimeWindowAllWindowedStream.process(new ProcessAllWindowFunction<UserAction, Object, TimeWindow>() {
            @Override
            public void process(ProcessAllWindowFunction<UserAction, Object, TimeWindow>.Context context, Iterable<UserAction> elements, Collector<Object> out) {
                pageView = 0L;
                uniqueView = new HashSet<>();
                for (UserAction next : elements) {
                    USER_ID.add(next.getUserId());
                    ITEM_ID.add(next.getItemId());
                    ITEM_CATEGORY_ID.add(next.getItemCategory());
                    uniqueView.add(next.getUserId());
                    pageView++;
                    all++;
                }
                Map<String, Object> resultMap = new HashMap<>();
                //pv和uv数，其中pv相当于总数目，uv相当于用户数，和下面的all和userNum区别在于这两个会周期性清空重新计算
                resultMap.put("pv", pageView);
                resultMap.put("uv", uniqueView.size());
                //数据条目数、用户数、商品数、商品类目数
                resultMap.put("allInfoNum", all);
                resultMap.put("userNum", USER_ID.size());
                resultMap.put("itemNum", ITEM_ID.size());
                resultMap.put("itemCategoryNum", ITEM_CATEGORY_ID.size());
                //浏览和购买前10的商品
                resultMap.put("top10BrowseItem", TOP10_BROWSE_ITEM);
                resultMap.put("top10BuyItem", TOP10_BROWSE_ITEM);
                //浏览->收藏->加购->购买数量
                resultMap.put("browse", ITEM_BROWSE_NUM.size());
                resultMap.put("collect", ITEM_COLLECT_NUM.size());
                resultMap.put("cart", ITEM_CART_NUM.size());
                resultMap.put("buy", ITEM_BUY_NUM.size());

                out.collect(resultMap);
            }
        });

        process.addSink(new RichSinkFunction<Object>() {
            private Connection conn = null;
            private PreparedStatement insertStmt = null;
            private PreparedStatement updateStmt = null;
            private PreparedStatement deleteStmt = null;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                conn = dataSource.getConnection();
            }

            @Override
            public void close() throws Exception {
                super.close();
            }

            @Override
            public void invoke(Object value, Context context) throws Exception {
                Map<String, Object> resultMap = (Map<String, Object>) value;
                updateStmt = conn.prepareStatement("update t_atomic_info set pv = ?, uv = ?, all_info_num = ?, user_num = ?, item_num = ?, item_category_num = ?, browse_num = ? , collect_num = ?, cart_num = ?, buy_num = ?");
                updateStmt.setLong(1, (Long) resultMap.get("pv"));
                updateStmt.setInt(2, (Integer) resultMap.get("uv"));

                updateStmt.setLong(3, (Long) resultMap.get("allInfoNum"));
                updateStmt.setInt(4, (Integer) resultMap.get("userNum"));
                updateStmt.setInt(5, (Integer) resultMap.get("itemNum"));
                updateStmt.setInt(6, (Integer) resultMap.get("itemCategoryNum"));

                updateStmt.setInt(7, (Integer) resultMap.get("browse"));
                updateStmt.setInt(8, (Integer) resultMap.get("collect"));
                updateStmt.setInt(9, (Integer) resultMap.get("cart"));
                updateStmt.setInt(10, (Integer) resultMap.get("buy"));

                updateStmt.execute();
                if (updateStmt.getUpdateCount() == 0) {
                    insertStmt = conn.prepareStatement("insert into t_atomic_info(pv, uv, all_info_num, user_num, item_num, item_category_num, browse_num, collect_num, cart_num, buy_num) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    insertStmt.setLong(1, (Long) resultMap.get("pv"));
                    insertStmt.setInt(2, (Integer) resultMap.get("uv"));

                    insertStmt.setLong(3, (Long) resultMap.get("allInfoNum"));
                    insertStmt.setInt(4, (Integer) resultMap.get("userNum"));
                    insertStmt.setInt(5, (Integer) resultMap.get("itemNum"));
                    insertStmt.setInt(6, (Integer) resultMap.get("itemCategoryNum"));

                    insertStmt.setInt(7, (Integer) resultMap.get("browse"));
                    insertStmt.setInt(8, (Integer) resultMap.get("collect"));
                    insertStmt.setInt(9, (Integer) resultMap.get("cart"));
                    insertStmt.setInt(10, (Integer) resultMap.get("buy"));

                    insertStmt.execute();
                }

                Thread thread = new Thread(() -> {
                    try {
                        HttpClientUtils.get("http://localhost:9091/info");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.start();

                synchronized (TOP10_BROWSE_ITEM) {
                    deleteStmt = conn.prepareStatement("delete from t_top10_browse");
                    deleteStmt.execute();
                    insertStmt = conn.prepareStatement("insert into t_top10_browse(item_id, browse_num) values (?, ?)");
                    TOP10_BROWSE_ITEM.forEach((k, v) -> {
                        try {
                            insertStmt.setInt(1, k);
                            insertStmt.setInt(2, v);
                            insertStmt.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    thread = new Thread(() -> {
                        try {
                            HttpClientUtils.get("http://localhost:9091/top10/browse");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    thread.start();
                }

                synchronized (TOP10_BUY_ITEM) {
                    deleteStmt = conn.prepareStatement("delete from t_top10_buy");
                    deleteStmt.execute();
                    insertStmt = conn.prepareStatement("insert into t_top10_buy(item_id, buy_num) values (?, ?)");
                    TOP10_BUY_ITEM.forEach((k, v) -> {
                        try {
                            insertStmt.setInt(1, k);
                            insertStmt.setInt(2, v);
                            insertStmt.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    thread = new Thread(){
                        @Override
                        public void run() {
                            try {
                                String result = HttpClientUtils.get("http://localhost:9091/top10/buy");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    thread.start();
                }
            }
        });

        //----------------------------------------------------------------------------------------------------------------------------------
        // 根据Key进行处理

        KeyedStream<UserAction, Integer> userActionIntegerKeyedStream =
                soso.keyBy((KeySelector<UserAction, Integer>) UserAction::getItemId);

        WindowedStream<UserAction, Integer, TimeWindow> window = userActionIntegerKeyedStream.window(new WindowAssigner<UserAction, TimeWindow>() {

            @Override
            public Collection<TimeWindow> assignWindows(UserAction element, long timestamp, WindowAssignerContext context) {
                // 获取当前元素的处理时间
                final long now = context.getCurrentProcessingTime();
                long size = WINDOW_SIZE;
                long globalOffset = 0L;
                long start = now - (now - globalOffset + size) % size;
                // 落在这个时间范围内的元素都会被分配到这个窗口中
                return Collections.singletonList(new TimeWindow(start, start + size));
            }

            @Override
            public Trigger<UserAction, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment env) {

                return new Trigger<UserAction, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(UserAction element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {
                        ctx.registerProcessingTimeTimer(window.maxTimestamp());
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        return TriggerResult.FIRE;
                    }

                    @Override
                    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
                        ctx.deleteProcessingTimeTimer(window.maxTimestamp());
                    }
                };
            }

            @Override
            public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
                return new TimeWindow.Serializer();
            }

            @Override
            public boolean isEventTime() {
                return false;
            }
        });

        window.process(new ProcessWindowFunction<UserAction, Object, Integer, TimeWindow>() {
            @Override
            public void process(Integer integer, ProcessWindowFunction<UserAction, Object, Integer, TimeWindow>.Context context, Iterable<UserAction> elements, Collector<Object> out) {
                for (UserAction userAction : elements) {
                    switch (userAction.getBehaviorType()) {
                        case BROWSE:
                            if (ITEM_BROWSE_NUM.containsKey(integer)) {
                                // 行为是浏览，且map对象中已经包含了该对象id
                                ITEM_BROWSE_NUM.put(integer, ITEM_BROWSE_NUM.get(integer) + 1);
                            } else {
                                // 行为是浏览，但map对象中不包含该对象id
                                ITEM_BROWSE_NUM.put(integer, 1);
                            }
                            break;
                        case COLLECT:
                            if (ITEM_COLLECT_NUM.containsKey(integer)) {
                                // 行为是浏览，且map对象中已经包含了该对象id
                                ITEM_COLLECT_NUM.put(integer, ITEM_COLLECT_NUM.get(integer) + 1);
                            } else {
                                // 行为是浏览，但map对象中不包含该对象id
                                ITEM_COLLECT_NUM.put(integer, 1);
                            }
                            break;
                        case CART:
                            if (ITEM_CART_NUM.containsKey(integer)) {
                                // 行为是浏览，且map对象中已经包含了该对象id
                                ITEM_CART_NUM.put(integer, ITEM_CART_NUM.get(integer) + 1);
                            } else {
                                // 行为是浏览，但map对象中不包含该对象id
                                ITEM_CART_NUM.put(integer, 1);
                            }
                            break;
                        case BUY:
                            if (ITEM_BUY_NUM.containsKey(integer)) {
                                // 行为是浏览，且map对象中已经包含了该对象id
                                ITEM_BUY_NUM.put(integer, ITEM_BUY_NUM.get(integer) + 1);
                            } else {
                                // 行为是浏览，但map对象中不包含该对象id
                                ITEM_BUY_NUM.put(integer, 1);
                            }
                            break;
                        default:
                            break;
                    }
                }
                // 维护浏览量最大的10个商品列表
                Map<Integer, Integer> top10browseResult = maintainTop10(integer, TOP10_BROWSE_ITEM, ITEM_BROWSE_NUM);
                // 维护购买量最大的10个商品列表
                Map<Integer, Integer> top10buyResult = maintainTop10(integer, TOP10_BUY_ITEM, ITEM_BUY_NUM);

                out.collect(top10browseResult);
                out.collect(top10buyResult);
            }
        });
        env.execute();
    }

    private synchronized static Map<Integer, Integer> maintainTop10(Integer itemId, Map<Integer, Integer> top10map, Map<Integer, Integer> allMap) {
        Integer minBuy = find10thItemId(top10map);
        if (allMap.get(itemId) == null) {
            return null;
        }
        try {
            if (top10map.size() < 10) {
                top10map.put(itemId, allMap.get(itemId));
            } else if (top10map.get(minBuy) < allMap.get(itemId)) {
                top10map.remove(minBuy);
                top10map.put(itemId, allMap.get(itemId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("min: " + top10map.get(minBuy));
            System.out.println("item: " + allMap.get(itemId));
            System.out.println("----------------------------key值为空----------------------------------");
            top10map.forEach((key, value) -> {
                System.out.println("key: " + key + " ,value: " + value);
            });
        }
        return new HashMap<>(top10map);

    }

    public static Integer find10thItemId(Map<Integer, Integer> map) {
        Integer min = null;
        Set<Integer> integers = map.keySet();
        for (Integer integer : integers) {
            try {
                if (min == null || map.get(integer) < map.get(min)) {
                    min = integer;
                }
            } catch (Exception e) {
                e.printStackTrace();

                System.out.println(map.get(integer));
                System.out.println(map.get(min));
                System.out.println("----------------------------查找最小值报空指针----------------------------------");
                map.forEach((key, value) -> {
                    System.out.println("key: " + key + " ,value: " + value);
                });
            }

        }
        // 如果有相同value的商品，那么先被遍历到的被返回
        if (min == null && map.size() > 0) {
            System.out.println("----------------------------map长度大于10，但是最小值为0----------------------------------");
            map.forEach((key, value) -> {
                System.out.println("key: " + key + " ,value: " + value);
            });
        }
        return min;
    }

}
