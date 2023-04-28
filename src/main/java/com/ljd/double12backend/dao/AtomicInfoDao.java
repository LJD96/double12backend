package com.ljd.double12backend.dao;

import com.ljd.double12backend.vo.AtomicInfo;

public interface AtomicInfoDao {
    int insert(AtomicInfo record);

    int insertSelective(AtomicInfo record);

    AtomicInfo getAtomicInfo();
}