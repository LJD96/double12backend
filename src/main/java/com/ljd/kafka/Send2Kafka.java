package com.ljd.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author Liu JianDong
 * @create 2023-02-28-14:06
 **/
public class Send2Kafka {
    private static final String SOURCE_PATH = "E:\\myResource\\斑马项目\\淘宝双十二大促用户购物行为数据可视化分析作业\\user_action_10000.csv";
    // private static final String SOURCE_PATH = "E:\\myResource\\斑马项目\\淘宝双十二大促用户购物行为数据可视化分析作业\\user_action.csv";
    private static final String TOPIC = "user_action_topic1";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // 1. 创建 kafka 生产者的配置对象
        Properties properties = new Properties();
        // 2. 给 kafka 配置对象添加配置信息：bootstrap.servers
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop1:9092,hadoop2:9092,hadoop3:9092");
        // key,value 序列化（必须）：key.serializer，value.serializer
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        // StringSerializer.class.getName() 等于 org.apache.kafka.common.serialization.StringSerializer
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer") ;
        // 3. 创建 kafka 生产者对象
        try(
                KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);
                BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(SOURCE_PATH))));
        ){
            String line = null;
            StringBuilder sb = null;
            while ((line = br.readLine()) != null) {
                sb = new StringBuilder();
                String[] split = line.split(",");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                Date parse = null;
                try {
                    parse = sdf.parse(split[split.length - 1]);
                } catch (ParseException parseException) {
                    continue;
                }

                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(parse);
                split[split.length - 1] = format;
                for (int i = 0; i < split.length; i++) {
                    if (i != split.length - 1) {
                        sb.append(split[i]).append(",");
                    }else{
                        sb.append(split[i]);
                    }
                }
                long t1 = System.currentTimeMillis();
                kafkaProducer.send(new ProducerRecord<>(TOPIC, sb.toString()));
                long t2 = System.currentTimeMillis();
                // System.out.println("before:" + (t2 - t1));
                Thread.sleep(5);
                // long t3 = System.currentTimeMillis();
                // System.out.println("after:" + (t3 - t2));
            }

            long endTime = System.currentTimeMillis();
            System.out.println("耗时：" +(endTime - startTime) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
