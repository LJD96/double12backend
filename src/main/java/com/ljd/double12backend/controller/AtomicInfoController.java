package com.ljd.double12backend.controller;

import com.alibaba.fastjson.JSON;
import com.ljd.double12backend.service.AtomicInfoService;
import com.ljd.double12backend.vo.AtomicInfo;
import com.ljd.double12backend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Liu JianDong
 * @create 2023-03-03-16:21
 **/
@RestController(value = "atomic")
public class AtomicInfoController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 声明SimpMessagingTemplate （或者使用@SendTo和@SendToUser注解）
     * SimpMessagingTemplate可以在需要用到推送的地方如Controller，service，Component等地方
     */
    @Resource
    private AtomicInfoService atomicInfoService;

    @GetMapping("/info")
    public String getAtomicInfo() {
        AtomicInfo atomicInfo = null;
        try{
            atomicInfo  = atomicInfoService.getAtomicInfo();
            Result result = new Result();
            result.setType("atomicInfo");
            result.setTypeId(1);
            result.setData(JSON.toJSONString(atomicInfo));
            simpMessagingTemplate.convertAndSend("/topic", result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return JSON.toJSONString(atomicInfo);
    }

}