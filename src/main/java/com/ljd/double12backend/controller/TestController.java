package com.ljd.double12backend.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Liu JianDong
 * @create 2023-03-08-11:16
 **/
@RestController(value = "test")
public class TestController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/test")
    public String test(){
        Map<String, Integer> map = new HashMap<>();
        map.put("test", new Random().nextInt(100));
        simpMessagingTemplate.convertAndSend("/test/162521/queue", JSON.toJSONString(map));
        simpMessagingTemplate.convertAndSendToUser("162521","/test/queue", JSON.toJSONString(map));
        return "OK";
    }
}
