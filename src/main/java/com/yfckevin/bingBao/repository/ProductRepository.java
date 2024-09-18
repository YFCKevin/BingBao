package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findAllByDeletionDateIsNullOrderByCreationDateDesc();
    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    List<Product> searchProductByName(String keyword);

    List<Product> findByIdIn(List<String> productIds);

    long countByDeletionDateIsNull();
}
