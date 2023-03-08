package com.ljd.double12backend.dao;

import com.ljd.double12backend.vo.Top10Browse;

import java.util.List;

public interface Top10BrowseDao {
    int insert(Top10Browse record);

    int insertSelective(Top10Browse record);

    List<Top10Browse> getTop10Browse();
}