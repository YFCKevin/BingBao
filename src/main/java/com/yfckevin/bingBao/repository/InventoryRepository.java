package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findByReceiveItemIdIn(List<String> receiveItemIds);

    List<Inventory> findByReceiveItemId(String receiveItemId);

    List<Inventory> findByReceiveItemIdAndUsedDateIsNullAndDeletionDateIsNull(String receiveItemId);
}
