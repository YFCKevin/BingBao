package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.ReceiveItem;
import com.yfckevin.bingBao.repository.ReceiveItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
