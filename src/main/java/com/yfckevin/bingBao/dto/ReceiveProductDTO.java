package com.yfckevin.bingBao.dto;

public class ReceiveProductDTO {
    private String expiryDate;
    private String productId;
    private int totalQuantity;  //數量 * 包裝單位 = 總數量
    private int quantity;      //數量
    private String storePlace;  //存放位置
    private String customExpiryDate;

    public String getCustomExpiryDate() {
        return customExpiryDate;
    }

    public void setCustomExpiryDate(String customExpiryDate) {
        this.customExpiryDate = customExpiryDate;
    }

    public String getStorePlace() {
        return storePlace;
    }

    public void setStorePlace(String storePlace) {
        this.storePlace = storePlace;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    @Override
    public String toString() {
        return "ReceiveProductDTO{" +
                "expiryDate='" + expiryDate + '\'' +
                ", productId='" + productId + '\'' +
                ", totalQuantity=" + totalQuantity +
                ", quantity=" + quantity +
                ", storePlace='" + storePlace + '\'' +
                '}';
    }
}
