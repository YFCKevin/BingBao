package com.yfckevin.bingBao.dto;

import com.yfckevin.bingBao.enums.MainCategory;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.enums.PackageUnit;
import com.yfckevin.bingBao.enums.SubCategory;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import org.springframework.data.mongodb.core.mapping.Document;

public class TempDetailDTO {
    private String id;
    private String name;    //名稱
    private String description; //食材描述
    private int price;
    private String mainCategoryLabel;  //食材種類 (水果、青菜、海鮮、藥品等等)
    private String packageUnitLabel;    //包裝單位 (包、瓶、個等等)
    private String subCategoryLabel;
    private String packageQuantity;     //包裝數量
    private String expiryDay;  //效期 (天數)
    private String overdueNotice;   //通知過期天數
    //for multi-add頁面用
    private PackageUnit packageUnit;
    private MainCategory mainCategory;
    private String title;   //食材建檔的nav名稱
    private SubCategory subCategory;
    private PackageForm packageForm;    //用於在食材建檔呈現用

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

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
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

    public String getMainCategoryLabel() {
        return mainCategoryLabel;
    }

    public void setMainCategoryLabel(String mainCategoryLabel) {
        this.mainCategoryLabel = mainCategoryLabel;
    }

    public String getPackageUnitLabel() {
        return packageUnitLabel;
    }

    public void setPackageUnitLabel(String packageUnitLabel) {
        this.packageUnitLabel = packageUnitLabel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubCategoryLabel() {
        return subCategoryLabel;
    }

    public void setSubCategoryLabel(String subCategoryLabel) {
        this.subCategoryLabel = subCategoryLabel;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }
}
