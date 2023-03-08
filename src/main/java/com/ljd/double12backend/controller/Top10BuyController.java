package com.ljd.double12backend.controller;

import com.alibaba.fastjson.JSON;
import com.ljd.double12backend.service.GetTop10BuyService;
import com.ljd.double12backend.vo.Result;
import com.ljd.double12backend.vo.Top10Buy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Liu JianDong
 * @create 2023-03-03-16:23
 **/
@RestController("/top10buy")
public class Top10BuyController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Resource
    private GetTop10BuyService service;

    @GetMapping("/top10/buy")
    private String getTop10Buy() {
        List<Top10Buy> top10Buy = service.getTop10Buy();
        Result result = new Result();
        result.setType("top10Buy");
        result.setTypeId(3);
        result.setData(JSON.toJSONString(top10Buy));
        simpMessagingTemplate.convertAndSend("/topic", result);
        return JSON.toJSONString(top10Buy);
    }
}
