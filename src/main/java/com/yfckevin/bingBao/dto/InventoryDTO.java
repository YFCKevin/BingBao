package com.yfckevin.bingBao.dto;

public class InventoryDTO {
    private String id;
    private String name;    //產品名稱
    private String serialNumber;    //產品序號
    private String receiveFormId; //收貨唯一編號
    private String receiveFormNumber; //收貨編號
    private String receiveItemId; //收貨明細唯一編號
    private String quantity;    //庫存數量
    private String usedDate;    //用產品日期
    private String storeDate;   //入庫日期
    private String storeNumber; //入庫編號
    private String expiryDate;   //有效日期
    private String expiryDay;  //效期 (天數)
    private String overdueNotice;   //通知過期天數
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getReceiveFormId() {
        return receiveFormId;
    }

    public void setReceiveFormId(String receiveFormId) {
        this.receiveFormId = receiveFormId;
    }

    public String getReceiveFormNumber() {
        return receiveFormNumber;
    }

    public void setReceiveFormNumber(String receiveFormNumber) {
        this.receiveFormNumber = receiveFormNumber;
    }

    public String getReceiveItemId() {
        return receiveItemId;
    }

    public void setReceiveItemId(String receiveItemId) {
        this.receiveItemId = receiveItemId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(String usedDate) {
        this.usedDate = usedDate;
    }

    public String getStoreDate() {
        return storeDate;
    }

    public void setStoreDate(String storeDate) {
        this.storeDate = storeDate;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getExpiryDay() {
        return expiryDay;
    }

    public void setExpiryDay(String expiryDay) {
        this.expiryDay = expiryDay;
    }

    public String getOverdueNotice() {
        return overdueNotice;
    }

    public void setOverdueNotice(String overdueNotice) {
        this.overdueNotice = overdueNotice;
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
        return "InventoryDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", receiveFormId='" + receiveFormId + '\'' +
                ", receiveFormNumber='" + receiveFormNumber + '\'' +
                ", receiveItemId='" + receiveItemId + '\'' +
                ", quantity='" + quantity + '\'' +
                ", usedDate='" + usedDate + '\'' +
                ", storeDate='" + storeDate + '\'' +
                ", storeNumber='" + storeNumber + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", expiryDay='" + expiryDay + '\'' +
                ", overdueNotice='" + overdueNotice + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
