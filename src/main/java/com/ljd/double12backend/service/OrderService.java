package com.ljd.double12backend.service;

import com.ljd.double12backend.vo.User;

import java.util.Map;

/**
 * @author Liu JianDong
 * @create 2023-03-28-9:04
 **/
public interface OrderService {
    boolean addOrder(User user, Map<Long, Integer> map) throws InterruptedException;

    boolean addOrderByRowLock(User user, Map<Long, Integer> map);

    boolean addOrderByRedisLock(User user, Map<Long, Integer> map);
}
