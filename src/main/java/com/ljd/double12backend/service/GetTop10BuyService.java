package com.ljd.double12backend.service;

import com.ljd.double12backend.vo.Top10Buy;

import java.util.List;

/**
 * @author Liu JianDong
 * @create 2023-03-06-15:10
 **/
public interface GetTop10BuyService {
    List<Top10Buy> getTop10Buy();
}
