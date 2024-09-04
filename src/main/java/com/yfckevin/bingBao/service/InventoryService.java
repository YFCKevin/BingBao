package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    List<Inventory> saveAll(List<Inventory> inventoryList);

    List<Inventory> findByIdIn(List<String> receiveItemIds);

    Map<String, Map<Long, List<Inventory>>> findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod();

    Map<String, Map<Long, List<Inventory>>> findInventoryExpiringSoonAndNoUsedAndNoDeleteAndInValidPeriod();

    Map<String, Map<Long, List<Inventory>>> findInventoryWithinValidityPeriodAndNoUsedAndNoDelete();

    Map<String, Map<Long, List<Inventory>>> searchByName(String keyword, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategory(String keyword, String mainCategory, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategory(String mainCategory, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory, String type);

    List<Inventory> findByReceiveItemId(String receiveItemId);
}
