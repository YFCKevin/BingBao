package com.yfckevin.bingBao.enums;

public enum PriorityType {
    URGENT(1,"緊急"),
    NORMAL(2,"普通");

    private PriorityType(){
    }

    private int value;
    private String label;
    private PriorityType(int value, String label){
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
