package com.ljd.double12backend.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ljd.double12backend.service.OrderItemService;
import com.ljd.double12backend.service.OrderService;
import com.ljd.double12backend.service.ProductService;
import com.ljd.double12backend.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Liu JianDong
 * @create 2023-03-28-9:03
 **/
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @PostMapping("/order/add")
    public String addOrder(@RequestBody String order) throws InterruptedException {
        JSONObject jsonObject = JSON.parseObject(order);
        String name = jsonObject.getString("name");
        String phone = jsonObject.getString("phone");
        User user = new User();
        user.setName(name);
        user.setPhone(phone);

        String orderInfo = jsonObject.getString("orderInfo");
        JSONArray productArray = JSON.parseArray(orderInfo);
        Map<Long, Integer> productMap = new HashMap<>();
        for (Object o : productArray) {
            JSONObject product = (JSONObject) o;
            Long id = product.getLong("id");
            Integer num = product.getInteger("num");
            productMap.put(id, num);
        }
        orderService.addOrder(user, productMap);
        return null;
    }

    @PostMapping("/order/add/row/lock")
    public String addOrderRowLock(@RequestBody String order){
        JSONObject jsonObject = JSON.parseObject(order);
        String name = jsonObject.getString("name");
        String phone = jsonObject.getString("phone");
        User user = new User();
        user.setName(name);
        user.setPhone(phone);

        String orderInfo = jsonObject.getString("orderInfo");
        JSONArray productArray = JSON.parseArray(orderInfo);
        Map<Long, Integer> productMap = new HashMap<>();
        for (Object o : productArray) {
            JSONObject product = (JSONObject) o;
            Long id = product.getLong("id");
            Integer num = product.getInteger("num");
            productMap.put(id, num);
        }
        orderService.addOrderByRowLock(user, productMap);
        return null;
    }

    @PostMapping("/order/add/redis/lock")
    public String addOrderRedisLock(@RequestBody String order){
        JSONObject jsonObject = JSON.parseObject(order);
        String name = jsonObject.getString("name");
        String phone = jsonObject.getString("phone");
        User user = new User();
        user.setName(name);
        user.setPhone(phone);

        String orderInfo = jsonObject.getString("orderInfo");
        JSONArray productArray = JSON.parseArray(orderInfo);
        Map<Long, Integer> productMap = new HashMap<>();
        for (Object o : productArray) {
            JSONObject product = (JSONObject) o;
            Long id = product.getLong("id");
            Integer num = product.getInteger("num");
            productMap.put(id, num);
        }
        orderService.addOrderByRedisLock(user, productMap);
        return null;
    }
}
