package com.ljd.double12backend.service.impl;

import com.ljd.double12backend.dao.OrderDao;
import com.ljd.double12backend.dao.OrderItemDao;
import com.ljd.double12backend.dao.ProductDao;
import com.ljd.double12backend.service.OrderService;
import com.ljd.double12backend.vo.Order;
import com.ljd.double12backend.vo.OrderItem;
import com.ljd.double12backend.vo.Product;
import com.ljd.double12backend.vo.User;

import com.ljd.utils.RedisDistributedLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * @author Liu JianDong
 * @create 2023-03-28-9:05
 **/
@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = Logger.getLogger(OrderServiceImpl.class.getName());
    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private ProductDao productDao;

    @Resource
    private DataSource dataSource;

    @Resource
    private JedisPool jedisPool;

    private static ReentrantLock lock=new ReentrantLock();

    private static ExecutorService service = Executors.newFixedThreadPool(20);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrder(User user, Map<Long, Integer> map) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                add( user,  map);
                lock.unlock();
            }
        });
        return true;
        }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrderByRowLock(User user, Map<Long, Integer> map) {
        Long count = 0L;

        List<Product> productList = new ArrayList<>();
        for (Long id : map.keySet()) {
            Product productDB = productDao.selectByPrimaryKeyRowLock(id);
            if((map.get(id) - productDB.getCount()) > 0){
                return false;
            }else {
                Product product = new Product();
                product.setId(id);
                product.setCount(productDB.getCount() - map.get(id));
                productDao.updateByPrimaryKeySelective(product);
                product.setPrice(productDB.getPrice());
                product.setCount(map.get(id));
                productList.add(product);
                count += map.get(id);
            }
        }

        Order order = new Order();
        order.setReceiverName(user.getName());
        order.setReceiverPhone(user.getPhone());
        order.setOrderAmount(count);
        orderDao.insertSelective(order);

        Long id = order.getId();
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(id);

        for (Product product : productList) {
            orderItem.setProductId(product.getId());
            orderItem.setPurchaseNum(product.getCount());
            orderItem.setPurchasePrice(product.getPrice());
            orderItemDao.insertSelective(orderItem);
        }
        return true;
    }

    @Override
    public boolean addOrderByRedisLock(User user, Map<Long, Integer> map) {
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = jedisPool.getResource();
            String lockKey = "mylock";
            int lockExpireTime = 30000;

            String requestId = UUID.randomUUID().toString();

            while (true) {
                if (RedisDistributedLock.tryGetDistributedLock(jedis, lockKey, requestId, lockExpireTime)) {
                    try {
                        // Execute the critical section here
                        result = add(user, map);
                        break;
                    } finally {
                        RedisDistributedLock.releaseDistributedLock(jedis, lockKey, requestId);
                    }
                } else {
                    // Failed to acquire the lock
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedisPool.returnResource(jedis);
        }
        return result;
    }


    public boolean add(User user, Map<Long, Integer> map){

            Long count = 0L;

            List<Product> productList = new ArrayList<>();
            for (Long id : map.keySet()) {
                Product productDB = productDao.selectByPrimaryKey(id);
                if((map.get(id) - productDB.getCount()) > 0){
                    return false;
                }else {
                    Product product = new Product();
                    product.setId(id);
                    product.setCount(productDB.getCount() - map.get(id));
                    productDao.updateByPrimaryKeySelective(product);
                    product.setPrice(productDB.getPrice());
                    product.setCount(map.get(id));
                    productList.add(product);
                    count += map.get(id);
                }
            }

            Order order = new Order();
            order.setReceiverName(user.getName());
            order.setReceiverPhone(user.getPhone());
            order.setOrderAmount(count);
            orderDao.insertSelective(order);

            Long id = order.getId();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(id);

            for (Product product : productList) {
                orderItem.setProductId(product.getId());
                orderItem.setPurchaseNum(product.getCount());
                orderItem.setPurchasePrice(product.getPrice());
                orderItemDao.insertSelective(orderItem);
            }
            return true;
        }
}
