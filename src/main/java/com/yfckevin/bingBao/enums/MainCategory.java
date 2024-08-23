package com.yfckevin.bingBao.enums;

public enum MainCategory {
    FRESH_FOOD(1, "生鮮食品"),
    FROZEN_FOOD(2, "冷凍食品"),
    DRY_GOODS(3, "乾貨"),
    BEVERAGE(4, "飲料"),
    MEDICINE(5, "藥品");

    private int value;
    private String label;
    private MainCategory(int value, String label) {
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

    @Override
    public String toString() {
        return label;
    }
}

