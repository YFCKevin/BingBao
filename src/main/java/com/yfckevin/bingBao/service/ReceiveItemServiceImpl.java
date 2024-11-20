package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.ReceiveItem;
import com.yfckevin.bingBao.repository.ReceiveItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReceiveItemServiceImpl implements ReceiveItemService{
    private final ReceiveItemRepository receiveItemRepository;

    public ReceiveItemServiceImpl(ReceiveItemRepository receiveItemRepository) {
        this.receiveItemRepository = receiveItemRepository;
    }

    @Override
    public List<ReceiveItem> findByIdIn(List<String> receiveItemIds) {
        return receiveItemRepository.findByIdIn(receiveItemIds);
    }

    @Override
    public ReceiveItem save(ReceiveItem receiveItem) {
        return receiveItemRepository.save(receiveItem);
    }

    @Override
    public Optional<ReceiveItem> findFirstByProductId(String id) {
        return receiveItemRepository.findFirstByProductId(id);
    }

    @Override
    public Optional<ReceiveItem> findById(String receiveItemId) {
        return receiveItemRepository.findById(receiveItemId);
    }
}
