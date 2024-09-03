package com.yfckevin.bingBao.entity;

import com.yfckevin.bingBao.enums.MainCategory;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.enums.PackageUnit;
import com.yfckevin.bingBao.enums.SubCategory;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tempDetail")
public class TempDetail {
    @Id
    private String id;
    private String name;    //名稱
    private String description; //商品描述
    private int price;
    private MainCategory mainCategory;  //商品種類 (水果、青菜、海鮮、藥品等等)
    private SubCategory subCategory;    //商品次種類
    private PackageUnit packageUnit;    //包裝單位 (包、瓶、個等等)
    private String packageQuantity;     //包裝數量
    private String overdueNotice;   //通知過期天數
    @Version
    private long version;

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

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getOverdueNotice() {
        return overdueNotice;
    }

    public void setOverdueNotice(String overdueNotice) {
        this.overdueNotice = overdueNotice;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }
}
