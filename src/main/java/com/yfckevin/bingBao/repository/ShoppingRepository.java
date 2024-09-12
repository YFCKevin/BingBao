package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.ShoppingItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShoppingRepository extends MongoRepository<ShoppingItem, String> {
    long countByDeletionDateIsNull();

    long countByDeletionDateIsNullAndPurchasedIsFalse();
}
