package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.entity.Supplier;
import com.yfckevin.bingBao.enums.*;
import org.springframework.data.mongodb.core.mapping.DBRef;

public class ShoppingItemDTO {
    private String id;
    private String name;    //名稱
    private String coverPath;   //圖片檔名
    @DBRef
    private Supplier supplier;  //供應商
    private String purchaseQuantity;    //採購數量
    private String demo;    //備忘錄or備註
    private PriorityType priorityType;  //優先級(緊急Urgent, 普通Normal)
    private String priorityTypeLabel;
    private String memberId;    //採購人(預設抓inventory的creator)
    private int price;
    private MainCategory mainCategory;  //食材種類 (水果、青菜、海鮮、藥品等等)
    private String mainCategoryLabel;
    private SubCategory subCategory;    //食材副總類
    private String subCategoryLabel;
    private PackageForm packageForm;    //包裝形式 (完整包裝、散裝)
    private String packageFormLabel;
    private PackageUnit packageUnit;    //包裝單位 (包、瓶、個等等)
    private String packageUnitLabel;
    private String packageQuantity;     //包裝數量
    private boolean purchased;    //是否已完成購買
    private String totalAmount;     //採購總數量
    private String supplierName;
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

    public String getCoverPath() {
        return coverPath;
    }

    public void setCovePath(String coverPath) {
        this.coverPath = coverPath;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(String purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
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

    public String getPriorityTypeLabel() {
        return priorityTypeLabel;
    }

    public void setPriorityTypeLabel(String priorityTypeLabel) {
        this.priorityTypeLabel = priorityTypeLabel;
    }

    public String getMainCategoryLabel() {
        return mainCategoryLabel;
    }

    public void setMainCategoryLabel(String mainCategoryLabel) {
        this.mainCategoryLabel = mainCategoryLabel;
    }

    public String getSubCategoryLabel() {
        return subCategoryLabel;
    }

    public void setSubCategoryLabel(String subCategoryLabel) {
        this.subCategoryLabel = subCategoryLabel;
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

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Override
    public String toString() {
        return "ShoppingItemDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", supplier=" + supplier +
                ", purchaseQuantity='" + purchaseQuantity + '\'' +
                ", demo='" + demo + '\'' +
                ", priorityType=" + priorityType +
                ", priorityTypeLabel='" + priorityTypeLabel + '\'' +
                ", memberId='" + memberId + '\'' +
                ", price=" + price +
                ", mainCategory=" + mainCategory +
                ", mainCategoryLabel='" + mainCategoryLabel + '\'' +
                ", subCategory=" + subCategory +
                ", subCategoryLabel='" + subCategoryLabel + '\'' +
                ", packageForm=" + packageForm +
                ", packageFormLabel='" + packageFormLabel + '\'' +
                ", packageUnit=" + packageUnit +
                ", packageUnitLabel='" + packageUnitLabel + '\'' +
                ", packageQuantity='" + packageQuantity + '\'' +
                ", purchased=" + purchased +
                ", totalAmount='" + totalAmount + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
