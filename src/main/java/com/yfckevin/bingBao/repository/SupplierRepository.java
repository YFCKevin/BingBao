package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Supplier;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SupplierRepository extends MongoRepository<Supplier, String> {
    List<Supplier> findAllByOrderByCreationDateDesc();
}
