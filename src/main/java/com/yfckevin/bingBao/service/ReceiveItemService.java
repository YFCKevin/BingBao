package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.ReceiveItem;

import java.util.List;

public interface ReceiveItemService {
    List<ReceiveItem> findByIdIn(List<String> receiveItemIds);

    ReceiveItem save(ReceiveItem receiveItem);
}
