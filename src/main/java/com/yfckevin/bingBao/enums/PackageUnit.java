package com.yfckevin.bingBao.enums;

public enum PackageUnit {
    EACH(1, "個"),
    BOX(2, "箱"),
    PACK(3, "包"),
    BOTTLE(4, "瓶"),
    BAG(5, "袋"),
    BARREL(6, "桶"),
    CASE(7, "盒"),
    CAN(8, "罐"),
    BUNDLE(9, "捆"),
    STRIP(10, "條");

    private int value;
    private String label;
    private PackageUnit(int value, String label) {
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

