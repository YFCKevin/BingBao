package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.dto.ReceiveFormDTO;
import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.entity.*;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.*;
import com.yfckevin.bingBao.utils.INUtil;
import com.yfckevin.bingBao.utils.RNUtil;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ReceiveController {
    Logger logger = LoggerFactory.getLogger(ReceiveController.class);
    private final ProductService productService;
    private final SupplierService supplierService;
    private final ReceiveFormService receiveFormService;
    private final ReceiveItemService receiveItemService;
    private final InventoryService inventoryService;
    private final ConfigProperties configProperties;
    private final SimpleDateFormat sdf;

    public ReceiveController(ProductService productService, SupplierService supplierService, ReceiveFormService receiveFormService, ReceiveItemService receiveItemService, InventoryService inventoryService, ConfigProperties configProperties, @Qualifier("sdf") SimpleDateFormat sdf) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.receiveFormService = receiveFormService;
        this.receiveItemService = receiveItemService;
        this.inventoryService = inventoryService;
        this.configProperties = configProperties;
        this.sdf = sdf;
    }

    /**
     * 新增或更新收貨、收貨明細和入庫
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/receive")
    public ResponseEntity<?> receive(@RequestBody ReceiveFormDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[receive]");
        }
        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            ReceiveForm receiveForm = new ReceiveForm();
            receiveForm.setCreationDate(sdf.format(new Date()));
            receiveForm.setCreator(member.getName());
            receiveForm.setReceiveDate(sdf.format(new Date()));
            receiveForm.setReceiveNumber(RNUtil.generateReceiveNumber());
            receiveForm.setStoreDate(sdf.format(new Date()));
            Optional<Supplier> supplierOpt = supplierService.findById(dto.getSupplierId());
            if (supplierOpt.isEmpty()) {
                resultStatus.setCode("C002");
                resultStatus.setMessage("查無供應商");
                return ResponseEntity.ok(receiveForm);
            } else {
                receiveForm.setSupplier(supplierOpt.get());
            }
            //組收貨明細
            List<ReceiveItem> receiveItemList = dto.getItemRequestDTOS()
                    .stream().map(item -> {
                        ReceiveItem receiveItem = new ReceiveItem();
                        receiveItem.setCreator(member.getName());
                        receiveItem.setCreationDate(sdf.format(new Date()));
                        receiveItem.setAmount(item.getQuantity());
                        receiveItem.setProductId(item.getProductId());
                        receiveItem.setExpiryDate(item.getExpiryDate());
                        return receiveItemService.save(receiveItem);
                    }).toList();
            receiveForm.setReceiveItems(receiveItemList);
            ReceiveForm savedReceiveForm = receiveFormService.save(receiveForm);
            //產品入庫
            final Map<String, Map<String, Integer>> itemIdExpiryDateAmountMap =
                    savedReceiveForm.getReceiveItems().stream()
                            .collect(Collectors.toMap(
                                    ReceiveItem::getId,
                                    receiveItem -> {
                                        Map<String, Integer> expiryDateAmountMap = new HashMap<>();
                                        expiryDateAmountMap.put(receiveItem.getExpiryDate(), receiveItem.getAmount());
                                        return expiryDateAmountMap;
                                    },
                                    (existingMap, newMap) -> {
                                        existingMap.putAll(newMap);
                                        return existingMap;
                                    }
                            ));
            final List<Inventory> inventoryList = savedReceiveForm.getReceiveItems()
                    .stream().map(item -> {
                        Inventory inventory = new Inventory();
                        inventory.setCreationDate(sdf.format(new Date()));
                        inventory.setCreator(member.getName());
                        inventory.setReceiveFormId(savedReceiveForm.getId());
                        inventory.setReceiveItemId(item.getId());
                        inventory.setStoreDate(sdf.format(new Date()));
                        inventory.setReceiveFormNumber(savedReceiveForm.getReceiveNumber());
                        inventory.setStoreNumber(INUtil.generateStoreNumber());
                        productService.findById(item.getProductId())
                                .ifPresent(product -> {
                                    inventory.setExpiryDay(product.getExpiryDay());
                                    inventory.setOverdueNotice(product.getOverdueNotice());
                                    final Map<String, Integer> expiryDateAmountMap = itemIdExpiryDateAmountMap.getOrDefault(item.getId(), null);
                                    if (expiryDateAmountMap != null) {
                                        for (Map.Entry<String, Integer> entry : expiryDateAmountMap.entrySet()) {
                                            inventory.setExpiryDate(entry.getKey());
                                            inventory.setQuantity(entry.getValue());
                                        }
                                    }
                                });
                        return inventory;
                    }).toList();
            inventoryService.saveAll(inventoryList);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(savedReceiveForm);
        } else {    //更新
            receiveFormService.findById(dto.getId())
                    .map(r -> {
                        r.setReceiveNumber(dto.getReceiveNumber());
                        r.setSupplier(supplierService.findById(dto.getSupplierId()).get());
                        r.setReceiveDate(dto.getReceiveDate());
                        r.setModificationDate(sdf.format(new Date()));
                        r.setModifier(member.getName());
                        final ReceiveForm savedReceiveForm = receiveFormService.save(r);
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                        resultStatus.setData(savedReceiveForm);
                        return resultStatus;
                    })
                    .orElseGet(() -> {
                        resultStatus.setCode("C003");
                        resultStatus.setMessage("查無收貨清單");
                        return resultStatus;
                    });
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 我的收貨列表
     * @param session
     * @return
     */
    @GetMapping("/allReceiveForms")
    public ResponseEntity<?> allReceiveForms (HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[allReceiveForms]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<ReceiveForm> receiveFormList = receiveFormService.findAllByDeletionDateIsNullOrderByCreationDateDesc();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(receiveFormList);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢單一收貨資訊
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/receiveFormInfo/{id}")
    public ResponseEntity<?> receiveFormInfo (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[receiveFormInfo]");
        }
        ResultStatus resultStatus = new ResultStatus();

        return receiveFormService.findById(id)
                .map(r -> {
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(r);
                    return ResponseEntity.ok(resultStatus);
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C003");
                    resultStatus.setMessage("查無收貨清單");
                    return ResponseEntity.ok(resultStatus);
                });

    }


    /**
     * 模糊查詢收貨資訊
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/receiveSearch")
    public ResponseEntity<?> receiveSearch (@RequestBody SearchDTO searchDTO, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[receiveSearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<ReceiveForm> receiveFormList = receiveFormService.findReceiveFormByCondition(searchDTO);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(receiveFormList);
        return ResponseEntity.ok(resultStatus);
    }

}
