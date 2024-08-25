package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierService {
    Optional<Supplier> findById(String supplierId);

    Supplier save(Supplier supplier);

    List<Supplier> findAllByOrderByCreationDateDesc();

    List<Supplier> findSupplierByCondition(String keyword);
}
