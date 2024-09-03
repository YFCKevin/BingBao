package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;

import java.util.List;

public interface InventoryService {
    List<Inventory> saveAll(List<Inventory> inventoryList);

    List<Inventory> findByIdIn(List<String> receiveItemIds);

    List<Inventory> findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod();

    List<Inventory> findInventoryExpiringSoonAndNoUsedAndNoDeleteAndInValidPeriod();

    List<Inventory> findInventoryWithinValidityPeriodAndNoUsedAndNoDelete();

    List<Inventory> searchByName(String keyword, String type);

    List<Inventory> searchByNameAndMainCategory(String keyword, String mainCategory, String type);

    List<Inventory> searchByMainCategory(String mainCategory, String type);

    List<Inventory> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory, String type);

    List<Inventory> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory, String type);

    List<Inventory> findByReceiveItemId(String receiveItemId);
}
