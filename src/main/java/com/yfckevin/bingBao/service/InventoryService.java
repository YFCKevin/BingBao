package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;

import java.util.List;

public interface InventoryService {
    void saveAll(List<Inventory> inventoryList);

    List<Inventory> findByIdIn(List<String> receiveItemIds);
}
