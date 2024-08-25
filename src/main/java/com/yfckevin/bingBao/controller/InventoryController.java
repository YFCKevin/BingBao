package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.dto.ReceiveItemRequestDTO;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class InventoryController {
    Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final InventoryService inventoryService;

    public InventoryController(@Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, InventoryService inventoryService) {
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.inventoryService = inventoryService;
    }

    /**
     * 使用產品對庫存數量進行扣減
     * @param itemDTOS
     * @param session
     * @return
     */
    @PostMapping("/useInventoryProduct")
    public ResponseEntity<?> useInventoryProduct (@RequestBody List<ReceiveItemRequestDTO> itemDTOS, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[useInventoryProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Inventory> inventoryList = inventoryService.findByIdIn(itemDTOS.stream().map(ReceiveItemRequestDTO::getReceiveItemId).toList());
        inventoryList = inventoryList.stream()
                .peek(i -> i.setUsedDate(sdf.format(new Date()))).toList();
        inventoryService.saveAll(inventoryList);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(inventoryList);
        return ResponseEntity.ok(resultStatus);
    }
}
