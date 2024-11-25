package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.dto.RecordDTO;
import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.dto.StoredRecord;
import com.yfckevin.bingBao.entity.Record;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.RecordService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RecordController {
    private final RecordService recordService;
    Logger logger = LoggerFactory.getLogger(RecordController.class);

    /**
     * 查詢操作記錄
     * @param recordService
     */
    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/recordSearch")
    public ResponseEntity<?> searchRecord(@RequestBody SearchDTO searchDTO, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[searchRecord]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Record> recordList = recordService.searchRecord(searchDTO);
        List<RecordDTO> recordDTOList = recordList.stream().map(RecordController::constructRecordDTO).toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(recordDTOList);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 取得收貨放進冰箱的細節資訊
     * @param recordId
     * @param session
     * @return
     */
    @GetMapping("/getRecordInfo/{recordId}")
    public ResponseEntity<?> getRecordInfo (@PathVariable String recordId, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[getRecordInfo]");
        }
        ResultStatus resultStatus = new ResultStatus();

        Record record = recordService.getRecordInfo(recordId);
        final List<StoredRecord> storedRecordList = record.getStoreRecordList().stream().peek(storedRecord -> {
            storedRecord.setStorePlace(StorePlace.valueOf(storedRecord.getStorePlace()).getLabel());
        }).toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(storedRecordList);
        return ResponseEntity.ok(resultStatus);
    }


    private static RecordDTO constructRecordDTO(Record record) {
        RecordDTO recordDTO = new RecordDTO();
        recordDTO.setId(record.getId());
        recordDTO.setOperator(record.getOperator());
        recordDTO.setCurrentTime(record.getCurrentTime());
        recordDTO.setAction(record.getAction().getLabel());
        recordDTO.setItem(record.getItem());
        recordDTO.setStoreRecordList(record.getStoreRecordList());
        recordDTO.setActionDetail(record.getActionDetail());
        return recordDTO;
    }
}
