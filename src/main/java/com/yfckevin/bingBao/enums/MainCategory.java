package com.yfckevin.bingBao.enums;

public enum MainCategory {
    MEAT(1, "肉品"),
    SEAFOOD(2, "海鮮"),
    VEGETABLES(3, "青菜"),
    FRUITS(4, "水果"),
    GRAINS(5, "穀物"),
    BEVERAGES(6, "飲料"),
    CONDIMENTS(7, "醬料"),
    SNACKS(8, "零食"),
    BAKERY(9, "烘焙食品"),
    MEDICINE(10, "藥品"),
    CANNED_FOOD(11, "罐頭食品"),
    SPICES(12, "香料"),
    OILS(13, "食用油"),
    SWEETS(14, "甜品"),
    DRIED_FOOD(15, "乾貨"),
    HEALTH_FOOD(16, "保健食品");

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

