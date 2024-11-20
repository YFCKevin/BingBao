package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.ReceiveItem;

import java.util.List;
import java.util.Optional;

public interface ReceiveItemService {
    List<ReceiveItem> findByIdIn(List<String> receiveItemIds);

    ReceiveItem save(ReceiveItem receiveItem);

    Optional<ReceiveItem> findFirstByProductId(String id);

    Optional<ReceiveItem> findById(String receiveItemId);
}
