package com.yfckevin.bingBao.enums;

public enum SubCategory {
    BEEF(1, "牛肉", MainCategory.MEAT),
    PORK(2, "豬肉", MainCategory.MEAT),
    CHICKEN(3, "雞肉", MainCategory.MEAT),
    EGG(4, "雞蛋", MainCategory.MEAT),
    LAMB(5, "羊肉", MainCategory.MEAT),
    DUCK(6, "鴨肉", MainCategory.MEAT),
    GOOSE(7, "鵝肉", MainCategory.MEAT),
    FISH(8, "魚", MainCategory.SEAFOOD),
    SHRIMP(9, "蝦", MainCategory.SEAFOOD);
    private int value;
    private String label;
    private MainCategory parent;

    SubCategory(int value, String label, MainCategory parent) {
        this.value = value;
        this.label = label;
        this.parent = parent;
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

    public MainCategory getParent() {
        return parent;
    }

    public void setParent(MainCategory parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return label;
    }
}
