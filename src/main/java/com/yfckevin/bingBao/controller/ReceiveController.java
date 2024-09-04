package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.*;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.enums.StorePlace;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    public ResponseEntity<?> receive(@RequestBody ReceiveRequestDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[receive]");
        }
        ResultStatus resultStatus = new ResultStatus();

        System.out.println("放進冰箱的內容物：" + dto);

        final String storeNumber = INUtil.generateStoreNumber();
        ReceiveForm receiveForm = new ReceiveForm();
        receiveForm.setCreationDate(sdf.format(new Date()));
//            receiveForm.setCreator(member.getName());
        receiveForm.setReceiveDate(sdf.format(new Date()));
        receiveForm.setReceiveNumber(RNUtil.generateReceiveNumber());
        receiveForm.setStoreDate(sdf.format(new Date()));
        receiveForm.setStoreNumber(storeNumber);
        Optional<Supplier> supplierOpt = supplierService.findById(dto.getSupplierId());
        if (supplierOpt.isEmpty()) {
            resultStatus.setCode("C002");
            resultStatus.setMessage("查無供應商");
            return ResponseEntity.ok(receiveForm);
        } else {
            receiveForm.setSupplier(supplierOpt.get());
        }

        //組收貨明細
        final List<ReceiveItem> receiveItemList = dto.getSelectedProducts().stream()
                .map(product -> {
                    ReceiveItem receiveItem = new ReceiveItem();
                    receiveItem.setCreationDate(sdf.format(new Date()));
                    receiveItem.setAmount(product.getQuantity());
                    receiveItem.setTotalAmount(product.getTotalQuantity());
                    receiveItem.setProductId(product.getProductId());
                    receiveItem.setExpiryDate(product.getExpiryDate());
                    receiveItem.setStorePlace(StorePlace.valueOf(product.getStorePlace())); //前端要加這個
                    return receiveItemService.save(receiveItem);
                }).toList();
        receiveForm.setReceiveItems(receiveItemList);
        ReceiveForm savedReceiveForm = receiveFormService.save(receiveForm);

        //食材放進冰箱(入庫)
        final Map<String, Map<String, Integer>> receiveItemIdExpiryDateAmountMap =
                savedReceiveForm.getReceiveItems().stream()
                        .collect(Collectors.toMap(
                                ReceiveItem::getId,
                                receiveItem -> {
                                    Map<String, Integer> expiryDateAmountMap = new HashMap<>();
                                    expiryDateAmountMap.put(receiveItem.getExpiryDate(), receiveItem.getTotalAmount());
                                    return expiryDateAmountMap;
                                },
                                (existingMap, newMap) -> {
                                    existingMap.putAll(newMap);
                                    return existingMap;
                                }
                        ));

        final List<Inventory> inventoryList = new ArrayList<>();
        for (ReceiveItem receiveItem : savedReceiveForm.getReceiveItems()) {
            Map<String, Integer> expiryDateAmountMap = receiveItemIdExpiryDateAmountMap.getOrDefault(receiveItem.getId(), null);
            if (expiryDateAmountMap != null) {
                for (Map.Entry<String, Integer> entry : expiryDateAmountMap.entrySet()) {
                    String expiryDate = entry.getKey();
                    int amount = entry.getValue();

                    for (int i = 0; i < amount; i++) {
                        Inventory inventory = new Inventory();
                        inventory.setCreationDate(sdf.format(new Date()));
                        inventory.setReceiveFormId(savedReceiveForm.getId());
                        inventory.setReceiveItemId(receiveItem.getId());
                        inventory.setStoreDate(sdf.format(new Date()));
                        inventory.setReceiveFormNumber(savedReceiveForm.getReceiveNumber());
                        inventory.setStoreNumber(storeNumber);
                        inventory.setExpiryDate(expiryDate);
                        //計算今日到有效期限的天數差 => 保鮮期
//                        LocalDate now = LocalDate.now();
//                        LocalDate parsedExpiryDate = LocalDate.parse(expiryDate);
//                        inventory.setExpiryDay(String.valueOf(ChronoUnit.DAYS.between(now, parsedExpiryDate)));
                        inventory.setStorePlace(receiveItem.getStorePlace());
                        productService.findById(receiveItem.getProductId())
                                .ifPresent(product -> {
                                    inventory.setProductId(product.getId());
                                    inventory.setOverdueNotice(product.getOverdueNotice());
                                    inventory.setPackageForm(product.getPackageForm());
                                    inventory.setPackageUnit(product.getPackageUnit());
                                    inventory.setPackageNumber(product.getPackageNumber());
                                    inventory.setPackageQuantity(product.getPackageQuantity());
                                });
                        inventoryList.add(inventory);
                    }
                }
            }
        }
        inventoryService.saveAll(inventoryList);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(savedReceiveForm);
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



    @GetMapping("/getGroupProducts")
    public ResponseEntity<?> getGroupProducts (HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[getGroupProducts]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final List<ProductDTO> productDTOList = productService.findAllByDeletionDateIsNullOrderByCreationDateDesc()
                .stream().map(this::constructProductDTO).toList();

        final Map<String, List<ProductDTO>> tempMap = productDTOList.stream()
                .collect(Collectors.groupingBy(ProductDTO::getPackageNumber));
        final Map<String, List<ProductDTO>> groupedProductMap = tempMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().stream()
                                .map(ProductDTO::getName)
                                .collect(Collectors.joining("<br>")) +
                                " (" + entry.getValue().get(0).getPackageNumber() + ")",  // 加入CreationDate到key中
                        Map.Entry::getValue
                ));
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(groupedProductMap);
        return ResponseEntity.ok(resultStatus);
    }



    @PostMapping("/getSelectedProduct")
    public ResponseEntity<?> getSelectedProduct (@RequestBody List<String> productIds, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[getSelectedProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final List<ProductDTO> productDTOList = productService.findByIdIn(productIds).stream().map(this::constructProductDTO).toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(productDTOList);
        return ResponseEntity.ok(resultStatus);
    }

    public ProductDTO constructProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setSerialNumber(product.getSerialNumber());
        if (PackageForm.BULK.equals(product.getPackageForm())) {
            dto.setPackageQuantity("1");
        } else {
            dto.setPackageUnit(product.getPackageUnit());
            dto.setPackageUnitLabel(product.getPackageUnit().getLabel());
            dto.setPackageQuantity(product.getPackageQuantity());
        }
        dto.setOverdueNotice(product.getOverdueNotice());
        dto.setMainCategoryLabel(product.getMainCategory().getLabel());
        dto.setModificationDate(product.getModificationDate());
        dto.setModifier(product.getModifier());
        dto.setDescription(product.getDescription());
        dto.setPackageForm(product.getPackageForm());
        dto.setPackageFormLabel(product.getPackageForm().getLabel());
        dto.setCreationDate(product.getCreationDate());
        dto.setCreator(product.getCreator());
        dto.setId(product.getId());
        dto.setCoverPath(configProperties.picShowPath + product.getCoverName());
        dto.setPackageNumber(product.getPackageNumber());
        return dto;
    }

}
