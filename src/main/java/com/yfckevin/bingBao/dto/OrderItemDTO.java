package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.entity.Product;

public class OrderItemDTO {
    private String id;
    private String productId;
    private int amount;
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

    @Override
    public String toString() {
        return "OrderItemDTO{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", amount=" + amount +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
