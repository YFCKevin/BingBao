package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.ProductDTO;
import com.yfckevin.bingBao.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product save(Product product);

    Optional<Product> findById(String id);

    List<Product> findAllByDeletionDateIsNullOrderByCreationDateDesc();

    void deleteById(String id);

    List<Product> saveAll(List<Product> productList);

    List<Product> findByIdIn(List<String> productIds);

    List<Product> searchProductByName(String keyword);

    List<Product> searchProductByNameAndMainCategory(String keyword, String mainCategory);

    List<Product> searchProductByMainCategory(String mainCategory);

    List<Product> searchProductByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory);

    List<Product> searchProductByMainCategoryAndSubCategory(String mainCategory, String subCategory);
}
