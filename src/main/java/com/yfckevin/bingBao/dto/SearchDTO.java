package com.yfckevin.bingBao.dto;

public class SearchDTO {
    private String keyword;
    private String startDate;
    private String endDate;
    private String supplierName;
    private String supplierId;
    private String mainCategory;
    private String subCategory;
    private String priority;
    private String type;    // today, soon, valid
    private String storePlace;

    public SearchDTO() {}
    public SearchDTO(String type) {
        this.keyword = "";
        this.mainCategory = "";
        this.subCategory = "";
        this.supplierId = "";
        this.storePlace = "";
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getStorePlace() {
        return storePlace;
    }

    public void setStorePlace(String storePlace) {
        this.storePlace = storePlace;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Override
    public String toString() {
        return "SearchDTO{" +
                "keyword='" + keyword + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", mainCategory='" + mainCategory + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", priority='" + priority + '\'' +
                ", type='" + type + '\'' +
                ", storePlace='" + storePlace + '\'' +
                '}';
    }
}
