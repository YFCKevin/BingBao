package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.enums.*;

public class InventoryDTO {
    private String id;
    private String name;    //食材名稱
    private String serialNumber;    //食材序號
    private String receiveFormId; //收貨唯一編號
    private String receiveFormNumber; //收貨編號
    private String receiveItemId; //收貨明細唯一編號
    private String usedDate;    //用食材日期
    private String storeDate;   //入庫日期
    private String storeNumber; //入庫批號
    private String expiryDate;   //有效日期
    private StorePlace storePlace;
    private String storePlaceLabel;
    private PackageForm packageForm;
    private String packageFormLabel;
    private PackageUnit packageUnit;
    private String packageUnitLabel;
    private String packageQuantity;
    private String packageNumber;
    private MainCategory mainCategory;
    private String mainCategoryLabel;
    private SubCategory subCategory;
    private String subCategoryLabel;
    private String overdueNotice;   //通知過期天數
    private String expiryTime;  //再過多久過期(天數or月數or年數)
    private String existedTime; //已存放多久(天數or月數or年數)
    private String noticeDate;  //預計何時通知(包含實際通知的年月日)
    private String totalAmount; //食材庫存總數
    private String coverPath;    //食材圖路徑(前端呈現用)
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

    public PackageForm getPackageForm() {
        return packageForm;
    }

    public void setPackageForm(PackageForm packageForm) {
        this.packageForm = packageForm;
    }

    public String getPackageFormLabel() {
        return packageFormLabel;
    }

    public void setPackageFormLabel(String packageFormLabel) {
        this.packageFormLabel = packageFormLabel;
    }

    public PackageUnit getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(PackageUnit packageUnit) {
        this.packageUnit = packageUnit;
    }

    public String getPackageUnitLabel() {
        return packageUnitLabel;
    }

    public void setPackageUnitLabel(String packageUnitLabel) {
        this.packageUnitLabel = packageUnitLabel;
    }

    public String getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(String packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getExistedTime() {
        return existedTime;
    }

    public void setExistedTime(String existedTime) {
        this.existedTime = existedTime;
    }

    public String getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(String noticeDate) {
        this.noticeDate = noticeDate;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getMainCategoryLabel() {
        return mainCategoryLabel;
    }

    public void setMainCategoryLabel(String mainCategoryLabel) {
        this.mainCategoryLabel = mainCategoryLabel;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public String getSubCategoryLabel() {
        return subCategoryLabel;
    }

    public void setSubCategoryLabel(String subCategoryLabel) {
        this.subCategoryLabel = subCategoryLabel;
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
                ", usedDate='" + usedDate + '\'' +
                ", storeDate='" + storeDate + '\'' +
                ", storeNumber='" + storeNumber + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", storePlace=" + storePlace +
                ", storePlaceLabel='" + storePlaceLabel + '\'' +
                ", packageForm=" + packageForm +
                ", packageFormLabel='" + packageFormLabel + '\'' +
                ", packageUnit=" + packageUnit +
                ", packageUnitLabel='" + packageUnitLabel + '\'' +
                ", packageQuantity='" + packageQuantity + '\'' +
                ", packageNumber='" + packageNumber + '\'' +
                ", mainCategory=" + mainCategory +
                ", mainCategoryLabel='" + mainCategoryLabel + '\'' +
                ", subCategory=" + subCategory +
                ", subCategoryLabel='" + subCategoryLabel + '\'' +
                ", overdueNotice='" + overdueNotice + '\'' +
                ", expiryTime='" + expiryTime + '\'' +
                ", existedTime='" + existedTime + '\'' +
                ", noticeDate='" + noticeDate + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
