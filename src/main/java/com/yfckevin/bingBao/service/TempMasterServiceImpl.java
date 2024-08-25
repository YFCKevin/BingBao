package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.TempMaster;
import com.yfckevin.bingBao.repository.TempMasterRepository;
import org.springframework.stereotype.Service;

@Service
public class TempMasterServiceImpl implements TempMasterService{
    private final TempMasterRepository tempMasterRepository;

    public TempMasterServiceImpl(TempMasterRepository tempMasterRepository) {
        this.tempMasterRepository = tempMasterRepository;
    }

    @Override
    public TempMaster save(TempMaster tempMaster) {
        return tempMasterRepository.save(tempMaster);
    }
}
