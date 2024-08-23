package com.yfckevin.bingBao.entity;

import com.yfckevin.bingBao.enums.*;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product")
public class Product {
    @Id
    private String id;
    private String serialNumber; //序號
    private String name;    //名稱
    private String description; //商品描述
    private String coverPath;   //圖片路徑
    private int price;
    private MainCategory mainCategory;  //商品種類 (水果、青菜、海鮮、藥品等等)
    private PackageForm packageForm;    //包裝形式 (完整包裝、散裝)
    private PackageUnit packageUnit;    //包裝單位 (包、瓶、個等等)
    private String packageQuantity;     //包裝數量
    private StorePlace storePlace;  //存放位置
    private String expiryDay;  //效期 (天數)
    private String overdueNotice;   //通知過期天數
    private String creationDate;
    private String modificationDate;
    private String deletionDate;
    private String creator;
    private String modifier;
    @Version
    private long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    public PackageForm getPackageForm() {
        return packageForm;
    }

    public void setPackageForm(PackageForm packageForm) {
        this.packageForm = packageForm;
    }

    public PackageUnit getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(PackageUnit packageUnit) {
        this.packageUnit = packageUnit;
    }

    public String getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(String packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public StorePlace getStorePlace() {
        return storePlace;
    }

    public void setStorePlace(StorePlace storePlace) {
        this.storePlace = storePlace;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
