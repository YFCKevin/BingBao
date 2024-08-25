package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.ReceiveForm;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReceiveFormRepository extends MongoRepository<ReceiveForm, String> {
    List<ReceiveForm> findAllByDeletionDateIsNullOrderByCreationDateDesc();
}
