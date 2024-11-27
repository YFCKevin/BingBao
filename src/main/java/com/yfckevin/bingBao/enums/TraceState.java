package com.yfckevin.bingBao.enums;

public enum TraceState {
    PRODUCT_STATE(1, "食材模板階段"),
    INVENTORY_STATE(2, "庫存階段"),
    SHOPPING_STATE(3, "回購階段"),
    SUPPLIER_STATE(4, "供應商階段");

    private int value;
    private String label;
    private TraceState(int value, String label) {
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
