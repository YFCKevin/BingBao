package com.yfckevin.bingBao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.bingBao.dto.ProductDTO;
import com.yfckevin.bingBao.entity.TempDetail;
import com.yfckevin.bingBao.entity.TempMaster;
import com.yfckevin.bingBao.exception.ResultStatus;

import java.util.List;

public interface OpenAiService {
    TempMaster completion(String textBuilder) throws JsonProcessingException;
}
