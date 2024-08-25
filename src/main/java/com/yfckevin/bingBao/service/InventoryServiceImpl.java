package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService{
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void saveAll(List<Inventory> inventoryList) {
        inventoryRepository.saveAll(inventoryList);
    }

    @Override
    public List<Inventory> findByIdIn(List<String> receiveItemIds) {
        return inventoryRepository.findByIdIn(receiveItemIds);
    }
}
