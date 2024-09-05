package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.ReceiveItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiveItemRepository extends MongoRepository<ReceiveItem, String> {
    List<ReceiveItem> findByIdIn(List<String> receiveItemIds);

    Optional<ReceiveItem> findFirstByProductId(String id);
}
