package com.ljd.double12backend.websocket.model;

/**
 * @author Liu JianDong
 * @create 2023-03-03-21:23
 **/
public class ResponseMessage {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponseMessage(String s) {
        this.message = s;
    }
}
