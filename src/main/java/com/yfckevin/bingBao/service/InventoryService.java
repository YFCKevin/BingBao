package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryService {
    List<Inventory> saveAll(List<Inventory> inventoryList);

    List<Inventory> findByReceiveItemIdIn(List<String> receiveItemIds);

    Map<String, Map<Long, List<Inventory>>> findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod();

    Map<String, Map<Long, List<Inventory>>> findInventoryExpiringSoonAndNoUsedAndNoDeleteAndInValidPeriod();

    Map<String, Map<Long, List<Inventory>>> findInventoryWithinValidityPeriodAndNoUsedAndNoDelete();

    Map<String, Map<Long, List<Inventory>>> searchByName(String keyword, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategory(String keyword, String mainCategory, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategory(String mainCategory, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory, String type);

    List<Inventory> findByReceiveItemId(String receiveItemId);

    List<Inventory> findInventoryNoticeDateIsBeforeExpiryDate();

    Optional<Inventory> findById(String id);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndStorePlace(String keyword, String type, String storePlace);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndStorePlace(String keyword, String mainCategory, String type, String storePlace);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndStorePlace(String mainCategory, String type, String storePlace);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategoryAndStorePlace(String keyword, String mainCategory, String subCategory, String type, String storePlace);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategoryAndStorePlace(String mainCategory, String subCategory, String type, String storePlace);

    Map<String, Map<Long, List<Inventory>>> searchByStorePlace(String type, String storePlace);

    List<Inventory> findInventoryWithExpiryDateInSevenDays();

    Map<String, Map<Long, List<Inventory>>> findAllAndDeletionDateIsNull();

    List<Inventory> findByReceiveItemIdAndUsedDateIsNull(String receiveItemId);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndSupplierId(String keyword, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSupplierId(String keyword, String mainCategory, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSupplierId(String mainCategory, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategoryAndSupplierId(String keyword, String mainCategory, String subCategory, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategoryAndSupplierId(String mainCategory, String subCategory, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndStorePlaceAndSupplierId(String keyword, String storePlace, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndStorePlaceAndSupplierId(String keyword, String mainCategory, String storePlace, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndStorePlaceAndSupplierId(String mainCategory, String storePlace, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategoryAndStorePlaceAndSupplierId(String keyword, String mainCategory, String subCategory, String storePlace, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategoryAndStorePlaceAndSupplierId(String mainCategory, String subCategory, String storePlace, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchByStorePlaceAndSupplierId(String storePlace, String supplierId, String type);

    Map<String, Map<Long, List<Inventory>>> searchBySupplierId(String supplierId, String type);
}
