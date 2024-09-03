package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.enums.StorePlace;

public class ReceiveItemDTO {
    private String id;
    private String productId;
    private int amount;
    private String expiryDate;  //產品有效期限日期
    private StorePlace storePlace;  //存放位置
    private String storePlaceLabel;
    private String creationDate;
    private String modificationDate;
    private String deletionDate;
    private String creator;
    private String modifier;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(String deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public StorePlace getStorePlace() {
        return storePlace;
    }

    public void setStorePlace(StorePlace storePlace) {
        this.storePlace = storePlace;
    }

    public String getStorePlaceLabel() {
        return storePlaceLabel;
    }

    public void setStorePlaceLabel(String storePlaceLabel) {
        this.storePlaceLabel = storePlaceLabel;
    }

    @Override
    public String toString() {
        return "ReceiveItemDTO{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", amount=" + amount +
                ", expiryDate='" + expiryDate + '\'' +
                ", storePlace=" + storePlace +
                ", storePlaceLabel='" + storePlaceLabel + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
