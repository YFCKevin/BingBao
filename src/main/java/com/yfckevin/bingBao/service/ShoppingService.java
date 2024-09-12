package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.ShoppingItem;

import java.util.List;
import java.util.Optional;

public interface ShoppingService {
    List<ShoppingItem> saveAll(List<ShoppingItem> shoppingItemList);

    List<ShoppingItem> searchByName(String keyword);

    List<ShoppingItem> searchByNameAndMainCategory(String keyword, String mainCategory);

    List<ShoppingItem> searchByMainCategory(String mainCategory);

    List<ShoppingItem> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory);

    List<ShoppingItem> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory);

    Optional<ShoppingItem> findById(String id);

    ShoppingItem save(ShoppingItem shoppingItem);

    long countByDeletionDateIsNull();

    long countByDeletionDateIsNullAndPurchasedIsFalse();

    List<ShoppingItem> searchByNameAndPriority(String keyword, String priority);

    List<ShoppingItem> searchByNameAndMainCategoryAndPriority(String keyword, String mainCategory, String priority);

    List<ShoppingItem> searchByMainCategoryAndPriority(String mainCategory, String priority);

    List<ShoppingItem> searchByNameAndMainCategoryAndSubCategoryAndPriority(String keyword, String mainCategory, String subCategory, String priority);

    List<ShoppingItem> searchByMainCategoryAndSubCategoryAndPriority(String mainCategory, String subCategory, String priority);

    List<ShoppingItem> searchByPriority(String priority);
}
