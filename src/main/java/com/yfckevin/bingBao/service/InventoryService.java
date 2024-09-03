package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;

import java.util.List;

public interface InventoryService {
    void saveAll(List<Inventory> inventoryList);

    List<Inventory> findByIdIn(List<String> receiveItemIds);

    List<Inventory> findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod();

    List<Inventory> findInventoryExpiringSoonAndNoUsedAndNoDeleteAndInValidPeriod();

    List<Inventory> findInventoryWithinValidityPeriodAndNoUsedAndNoDelete();

    List<Inventory> todaySearchByName(String keyword, String type);

    List<Inventory> todaySearchByNameAndMainCategory(String keyword, String mainCategory, String type);

    List<Inventory> todaySearchByMainCategory(String mainCategory, String type);

    List<Inventory> todaySearchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory, String type);

    List<Inventory> todaySearchByMainCategoryAndSubCategory(String mainCategory, String subCategory, String type);
}
