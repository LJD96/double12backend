package com.ljd.double12backend.service;

import com.ljd.double12backend.vo.Top10Browse;

import java.util.List;

/**
 * @author Liu JianDong
 * @create 2023-03-06-15:10
 **/
public interface GetTop10BrowseService {
    List<Top10Browse> getTop10Browse();
}
