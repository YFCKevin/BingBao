package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.ProductDTO;
import com.yfckevin.bingBao.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product save(Product product);

    Optional<Product> findById(String id);

    List<Product> findAllByOrderByCreationDateDesc();

    void deleteById(String id);

    List<Product> searchProduct(String keyword);
}
