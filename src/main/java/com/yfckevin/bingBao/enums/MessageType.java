package com.yfckevin.bingBao.enums;

public enum MessageType {
    text(1, "文字"),
    image(2, "圖片"),
    video(3, "影片"),
    audio(4, "音檔"),
    location(5, "地理位置"),
    sticker(6, "貼圖");

    private int value;
    private String label;

    private MessageType(){
    }

    private MessageType(int value, String label) {
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

