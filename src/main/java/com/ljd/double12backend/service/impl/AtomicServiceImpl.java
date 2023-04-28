package com.ljd.double12backend.service.impl;

import com.ljd.double12backend.dao.AtomicInfoDao;
import com.ljd.double12backend.service.AtomicInfoService;

import com.ljd.double12backend.vo.AtomicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Liu JianDong
 * @create 2023-03-03-16:58
 **/
@Service
public class AtomicServiceImpl implements AtomicInfoService {

    @Resource
    private AtomicInfoDao atomicInfoDao;

    @Override
    public AtomicInfo getAtomicInfo() {
        AtomicInfo atomicInfo = atomicInfoDao.getAtomicInfo();
        return atomicInfo;
    }
}
