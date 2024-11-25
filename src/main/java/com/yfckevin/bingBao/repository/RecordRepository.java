package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Record;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecordRepository extends MongoRepository<Record, String> {
}
