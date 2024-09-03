package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.TempMaster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TempMasterRepository extends MongoRepository<TempMaster, String> {
    List<TempMaster> findAllByOrderByCreationDateDesc();
}
