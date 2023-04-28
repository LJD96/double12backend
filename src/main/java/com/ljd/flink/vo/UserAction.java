package com.ljd.flink.vo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Liu JianDong
 * @create 2023-02-28-14:31
 **/
public class UserAction {
    /**
     * 用户id
     */
    private int userId;
    /**
     * 商品id
     */
    private int itemId;
    /**
     * 用户行为 1，2，3，4分别代表浏览、收藏、加购物车、购买
     */
    private int behaviorType;
    /**
     * 商品分类
     */
    private int itemCategory;
    /**
     * 行为时间
     */
    private Date time;

    public UserAction() {
    }

    public UserAction(int userId, int itemId, int behaviorType, int itemCategory, Date time) {
        this.userId = userId;
        this.itemId = itemId;
        this.behaviorType = behaviorType;
        this.itemCategory = itemCategory;
        this.time = time;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(int behaviorType) {
        this.behaviorType = behaviorType;
    }

    public int getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(int itemCategory) {
        this.itemCategory = itemCategory;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "UserAction{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", behaviorType=" + behaviorType +
                ", itemCategory=" + itemCategory +
                ", time=" + sdf.format(time) +
                '}';
    }
}
