package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.entity.ReceiveItem;
import com.yfckevin.bingBao.entity.Supplier;

import java.util.ArrayList;
import java.util.List;

public class ReceiveFormDTO {
    private String id;
    private String receiveNumber; //收貨編號
    private String receiveDate;   //收貨日期
    private String storeNumber; //入庫編號
    private String storeDate;   //入庫日期
    private String supplierId;  //食材供應商編號
    private List<ReceiveItemRequestDTO> itemRequestDTOS = new ArrayList<>();    //收貨食材明細編號
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

    public String getReceiveNumber() {
        return receiveNumber;
    }

    public void setReceiveNumber(String receiveNumber) {
        this.receiveNumber = receiveNumber;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getStoreDate() {
        return storeDate;
    }

    public void setStoreDate(String storeDate) {
        this.storeDate = storeDate;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public List<ReceiveItemRequestDTO> getItemRequestDTOS() {
        return itemRequestDTOS;
    }

    public void setItemRequestDTOS(List<ReceiveItemRequestDTO> itemRequestDTOS) {
        this.itemRequestDTOS = itemRequestDTOS;
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
        return "ReceiveFormDTO{" +
                "id='" + id + '\'' +
                ", receiveNumber='" + receiveNumber + '\'' +
                ", receiveDate='" + receiveDate + '\'' +
                ", storeNumber='" + storeNumber + '\'' +
                ", storeDate='" + storeDate + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", itemRequestDTOS=" + itemRequestDTOS +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
