package com.yfckevin.bingBao.enums;

public enum StorePlace {
    HUALIEN_ALL(1, "花蓮全部"),
    HUALIEN_REF_2F_LEFT(2, "花蓮2樓左側冰箱"),
    HUALIEN_REF_2F_RIGHT(3, "花蓮2樓右側冰箱"),
    HUALIEN_REF_1F_RIGHT(4, "花蓮1樓冰箱"),
    HUALIEN_OTHER(5, "花蓮其他"),
    TAIPEI_REF(6, "台北冰箱"),
    TAIPEI_OTHER(7, "台北其他");

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
