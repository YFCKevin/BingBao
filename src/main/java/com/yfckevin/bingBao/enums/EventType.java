package com.yfckevin.bingBao.enums;

public enum EventType {
    message(1, "訊息"),
    follow(2, "關注"),
    unfollow(3, "取消關注"),
    join(4, "加入"),
    leave(5, "離開"),
    postback(6, "回傳"),
    beacon(7, "Beacon"),
    account_link(8, "帳號連結"),
    member_joined(9, "成員加入"),
    member_left(10, "成員離開");

    private int value;
    private String label;

    private EventType() {
    }

    private EventType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

