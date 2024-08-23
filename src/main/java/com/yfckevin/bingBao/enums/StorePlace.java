package com.yfckevin.bingBao.enums;

public enum StorePlace {
    HUALIEN_REF_2F_LEFT(1, "花蓮2樓左側冰箱"),
    HUALIEN_REF_2F_RIGHT(2, "花蓮2樓右側冰箱"),
    HUALIEN_REF_1F_RIGHT(3, "花蓮1樓冰箱"),
    TAIPEI_REF(4, "台北冰箱");

    private StorePlace(){
    }

    private int value;
    private String label;
    private StorePlace(int value,String label){
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
