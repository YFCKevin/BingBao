package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.enums.*;
import jakarta.persistence.Version;
import org.springframework.web.multipart.MultipartFile;

public class ProductDTO {
    private String id;
    private String serialNumber; //序號
    private String name;    //名稱
    private String description; //食材描述
    private MultipartFile multipartFile;    //圖片檔案
    private String copyCoverName;
    private String coverPath;    //圖路徑(前端呈現用)
    private int price;
    private MainCategory mainCategory;  //食材種類 (水果、青菜、海鮮、藥品等等)
    private String mainCategoryLabel;
    private PackageForm packageForm;    //包裝形式 (完整包裝、散裝)
    private String packageFormLabel;
    private PackageUnit packageUnit;    //包裝單位 (包、瓶、個等等)
    private String packageUnitLabel;
    private SubCategory subCategory;
    private String subCategoryLabel;
    private String packageQuantity;     //包裝數量
    private String expiryDay;  //效期 (年月日)
    private String shelfLife;   //保質天數
    private String overdueNotice;   //通知過期天數
    private String packageNumber;   //批次建檔編號
    private boolean addShoppingList;    //是否加入購物清單
    private String inventoryAlert;  //通知購物的庫存量警報
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

    public String getMainCategoryLabel() {
        return mainCategoryLabel;
    }

    public void setMainCategoryLabel(String mainCategoryLabel) {
        this.mainCategoryLabel = mainCategoryLabel;
    }

    public String getPackageFormLabel() {
        return packageFormLabel;
    }

    public void setPackageFormLabel(String packageFormLabel) {
        this.packageFormLabel = packageFormLabel;
    }

    public String getPackageUnitLabel() {
        return packageUnitLabel;
    }

    public void setPackageUnitLabel(String packageUnitLabel) {
        this.packageUnitLabel = packageUnitLabel;
    }
    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
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

    public String getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(String shelfLife) {
        this.shelfLife = shelfLife;
    }

    public boolean isAddShoppingList() {
        return addShoppingList;
    }

    public void setAddShoppingList(boolean addShoppingList) {
        this.addShoppingList = addShoppingList;
    }

    public String getInventoryAlert() {
        return inventoryAlert;
    }

    public void setInventoryAlert(String inventoryAlert) {
        this.inventoryAlert = inventoryAlert;
    }

    public String getCopyCoverName() {
        return copyCoverName;
    }

    public void setCopyCoverName(String copyCoverName) {
        this.copyCoverName = copyCoverName;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id='" + id + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", multipartFile=" + multipartFile +
                ", copyCoverName='" + copyCoverName + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", price=" + price +
                ", mainCategory=" + mainCategory +
                ", mainCategoryLabel='" + mainCategoryLabel + '\'' +
                ", packageForm=" + packageForm +
                ", packageFormLabel='" + packageFormLabel + '\'' +
                ", packageUnit=" + packageUnit +
                ", packageUnitLabel='" + packageUnitLabel + '\'' +
                ", subCategory=" + subCategory +
                ", subCategoryLabel='" + subCategoryLabel + '\'' +
                ", packageQuantity='" + packageQuantity + '\'' +
                ", expiryDay='" + expiryDay + '\'' +
                ", shelfLife='" + shelfLife + '\'' +
                ", overdueNotice='" + overdueNotice + '\'' +
                ", packageNumber='" + packageNumber + '\'' +
                ", addShoppingList=" + addShoppingList +
                ", inventoryAlert='" + inventoryAlert + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
