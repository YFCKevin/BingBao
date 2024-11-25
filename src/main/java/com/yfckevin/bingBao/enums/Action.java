package com.yfckevin.bingBao.enums;

public enum Action {
    IMPORT_IMAGE(1, "匯入照片"),
    CREATE_PRODUCT(2, "創建食材模板"),
    EDIT_PRODUCT(3, "編輯食材模板"),
    DELETE_PRODUCT(4, "刪除食材模板"),
    QUERY_PRODUCT(5, "查詢食材模板"),
    RECEIVE(6, "食材放入冰箱"),
    EDIT_INVENTORY_AMOUNT(7, "修改庫存使用數量"),
    EDIT_INVENTORY_EXPIRYDATE(8, "修改庫存有效期限"),
    CLONE_INVENTORY(9, "增加庫存數量"),
    EDIT_INVENTORY_STOREPLACE(10, "變更庫存存放位置"),
    DELETE_INVENTORY(11, "刪除庫存"),
    QUERY_INVENTORY(12, "查詢庫存"),
    EXPORT_EXCEL(13, "匯出excel"),
    CREATE_SUPPLIER(14, "新增供應商"),
    EDIT_SUPPLIER(15, "修改供應商"),
    DELETE_SUPPLIER(16, "刪除供應商"),
    QUERY_SUPPLIER(17, "查詢供應商");

    private int value;
    private String label;
    private Action(int value, String label) {
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
