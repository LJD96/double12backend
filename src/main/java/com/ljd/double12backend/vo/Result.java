package com.ljd.double12backend.vo;

/**
 * @author Liu JianDong
 * @create 2023-03-06-20:24
 **/
public class Result {
    private String type;
    private int typeId;
    private String data;

    @Override
    public String toString() {
        return "Result{" +
                "type='" + type + '\'' +
                ", typeId=" + typeId +
                ", data='" + data + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
