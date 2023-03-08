package com.ljd.double12backend.controller;

import com.alibaba.fastjson.JSON;
import com.ljd.double12backend.service.GetTop10BrowseService;
import com.ljd.double12backend.vo.Result;
import com.ljd.double12backend.vo.Top10Browse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Liu JianDong
 * @create 2023-03-03-16:22
 **/
@RestController("/top10browse")
public class Top10BrowseController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Resource
    private GetTop10BrowseService service;

    @GetMapping("/top10/browse")
    private String getTop10Browse() {
        List<Top10Browse> top10Browse = service.getTop10Browse();
        Result result = new Result();
        result.setType("top10Browse");
        result.setTypeId(2);
        result.setData(JSON.toJSONString(top10Browse));
        simpMessagingTemplate.convertAndSend("/topic", result);
        return JSON.toJSONString(top10Browse);
    }
}
