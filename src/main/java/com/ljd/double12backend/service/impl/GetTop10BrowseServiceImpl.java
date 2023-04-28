package com.ljd.double12backend.service.impl;

import com.ljd.double12backend.dao.Top10BrowseDao;
import com.ljd.double12backend.service.GetTop10BrowseService;
import com.ljd.double12backend.vo.Top10Browse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Liu JianDong
 * @create 2023-03-06-15:10
 **/
@Service
public class GetTop10BrowseServiceImpl implements GetTop10BrowseService {
    @Resource
    Top10BrowseDao top10BrowseDao;

    @Override
    public List<Top10Browse> getTop10Browse() {
        return top10BrowseDao.getTop10Browse();
    }
}
