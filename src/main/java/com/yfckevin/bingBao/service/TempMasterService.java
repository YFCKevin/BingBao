package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.TempMaster;

import java.util.List;
import java.util.Optional;

public interface TempMasterService {
    TempMaster save(TempMaster tempMaster);

    Optional<TempMaster> findById(String id);

    List<TempMaster> findAllByOrderByCreationDateDesc();
}
