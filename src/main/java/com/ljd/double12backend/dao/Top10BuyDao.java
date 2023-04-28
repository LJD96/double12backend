package com.ljd.double12backend.dao;

import com.ljd.double12backend.vo.Top10Buy;

import java.util.List;

public interface Top10BuyDao {
    int insert(Top10Buy record);

    int insertSelective(Top10Buy record);

    List<Top10Buy> getTop10Buy();
}