package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.entity.ReceiveForm;

import java.util.List;
import java.util.Optional;

public interface ReceiveFormService {
    ReceiveForm save(ReceiveForm receiveForm);

    Optional<ReceiveForm> findById(String id);

    List<ReceiveForm> findAllByDeletionDateIsNullOrderByCreationDateDesc();

    List<ReceiveForm> findReceiveFormByCondition(SearchDTO searchDTO);
}
