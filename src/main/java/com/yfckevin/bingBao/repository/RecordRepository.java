package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Record;
import com.yfckevin.bingBao.enums.TraceState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecordRepository extends MongoRepository<Record, String> {

    List<Record> findByProductIdAndTraceState(String productId, TraceState traceState);

    List<Record> findByReceiveItemIdAndTraceState(String receiveItemId, TraceState traceState);

    Optional<Record> findByTempMasterId(String tempMasterId);

    List<Record> findByShoppingItemIdAndTraceState(String shoppingItemId, TraceState traceState);
}
