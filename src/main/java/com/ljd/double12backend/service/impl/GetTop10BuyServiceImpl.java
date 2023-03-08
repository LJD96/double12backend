package com.ljd.double12backend.service.impl;

import com.ljd.double12backend.dao.Top10BuyDao;
import com.ljd.double12backend.service.GetTop10BuyService;
import com.ljd.double12backend.vo.Top10Buy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Liu JianDong
 * @create 2023-03-06-15:11
 **/
@Service
public class GetTop10BuyServiceImpl implements GetTop10BuyService {
    @Resource
    Top10BuyDao top10BuyDao;

    @Override
    public List<Top10Buy> getTop10Buy() {
        return top10BuyDao.getTop10Buy();
    }
}
