package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.TempMaster;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TempMasterRepository extends MongoRepository<TempMaster, String> {
}
