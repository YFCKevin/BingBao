package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.*;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.enums.PriorityType;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.*;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yfckevin.bingBao.utils.DateUtil.genDateFormatted;
import static com.yfckevin.bingBao.utils.DateUtil.genNoticeDateFormatted;

@RestController
public class ShoppingListController {
    Logger logger = LoggerFactory.getLogger(ShoppingListController.class);
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final ShoppingService shoppingService;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final ReceiveFormService receiveFormService;
    private final RecordService recordService;

    public ShoppingListController(@Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, ShoppingService shoppingService, InventoryService inventoryService, ProductService productService, ReceiveFormService receiveFormService, RecordService recordService) {
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.shoppingService = shoppingService;
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.receiveFormService = receiveFormService;
        this.recordService = recordService;
    }

//    @GetMapping("/addToShoppingList")
    /**
     * 每天24點執行一次，低於庫存水平線的食材加入購物清單
     * @return
     */
    @Scheduled(cron = "0 0 0 * * *")
    public ResponseEntity<?> addToShoppingList() {
        logger.info("低於庫存水平線的食材加入購物清單");
        ResultStatus resultStatus = new ResultStatus();
        Map<String, Map<Long, List<Inventory>>> itemIdAmountInventoryMap = inventoryService.findAllAndDeletionDateIsNull();
        final Map<String, Long> receiveItemIdInventoryAmountMap = itemIdAmountInventoryMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream().flatMap(List::stream).count()
                ));
        //改用groupingBy避免相同receiveItemId被壓縮成一筆Inventory，因為後續要用inventoryList變更addShoppingList屬性
        final List<Inventory> inventoryList = itemIdAmountInventoryMap.values().stream()
                .flatMap(innerMap -> innerMap.values().stream())
                .flatMap(List::stream)
                .filter(inventory -> !inventory.isAddShoppingList())  // 只保留尚未放進回購清單的庫存食材
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId
                ))
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();
        final List<String> productIds = inventoryList.stream().map(Inventory::getProductId).toList();
        final Map<String, Product> tempProductMap = productService.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<InventoryDTO> finalInventory = inventoryList.stream()
                //相同productId的Inventory只取第一個，因為後續要用finalInventory儲存shoppingList，避免相同食材儲存多筆
                .collect(Collectors.toMap(
                        Inventory::getProductId,
                        Function.identity(),
                        (existing, replacement) -> existing
                        ))
                .values()
                .stream()
                .map(inventory -> {
                    final Long totalAmount = receiveItemIdInventoryAmountMap.get(inventory.getReceiveItemId());
                    final Product product = tempProductMap.get(inventory.getProductId());
                    return constructInventoryDTO(inventory, product, String.valueOf(totalAmount));
                })
                .sorted(Comparator.comparing(InventoryDTO::getExpiryDate))
                .toList();

        final List<String> receiveFormIds = finalInventory.stream().map(InventoryDTO::getReceiveFormId).toList();
        System.out.println(finalInventory);
        List<ReceiveForm> receiveFormList = receiveFormService.findByIdIn(receiveFormIds);
        final Map<String, Supplier> receiveFormIdSupplierMap = receiveFormList.stream()
                .filter(receiveForm -> receiveForm.getSupplier() != null)   //去掉放進冰箱時並未選擇供應商的
                .collect(Collectors.toMap(ReceiveForm::getId, ReceiveForm::getSupplier));

        final List<ShoppingItem> shoppingItemList = finalInventory.stream()
                .map(inventoryDTO -> {
                    ShoppingItem shoppingItem = new ShoppingItem();
                    shoppingItem.setName(inventoryDTO.getName());
                    shoppingItem.setPackageForm(inventoryDTO.getPackageForm());
                    shoppingItem.setPackageUnit(inventoryDTO.getPackageUnit());
                    shoppingItem.setMainCategory(inventoryDTO.getMainCategory());
                    shoppingItem.setPackageQuantity(inventoryDTO.getPackageQuantity());
                    shoppingItem.setSubCategory(inventoryDTO.getSubCategory());
                    shoppingItem.setCoverPath(
                            inventoryDTO.getCoverPath().endsWith("/null")
                                    ? null
                                    : inventoryDTO.getCoverPath().substring(inventoryDTO.getCoverPath().lastIndexOf('/') + 1)
                    );
                    shoppingItem.setPriorityType(PriorityType.NORMAL);
                    shoppingItem.setMemberId(inventoryDTO.getCreator());
                    shoppingItem.setPurchaseQuantity("1");  //預設採購數量為1
                    shoppingItem.setCreationDate(sdf.format(new Date()));
                    shoppingItem.setSupplier(receiveFormIdSupplierMap.getOrDefault(inventoryDTO.getReceiveFormId(), null));
                    shoppingItem.setPurchased(false);
                    shoppingItem.setProductId(inventoryDTO.getProductId());
                    return shoppingItem;
                }).toList();
        List<ShoppingItem> savedShoppingList = shoppingService.saveAll(shoppingItemList);

        //將這批放進回購清單的庫存食材標記 "已放入"
        final List<Inventory> markedInventoryList = inventoryList.stream().peek(inventory -> inventory.setAddShoppingList(true)).toList();
        inventoryService.saveAll(markedInventoryList);

        recordService.addToShoppingList(markedInventoryList, savedShoppingList);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 模糊查詢回購品項
     *
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/shoppingItemSearch")
    public ResponseEntity<?> shoppingItemSearch(@RequestBody SearchDTO searchDTO, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[shoppingItemSearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final String keyword = searchDTO.getKeyword().trim();
        final String mainCategory = searchDTO.getMainCategory();
        final String subCategory = searchDTO.getSubCategory();
        final String priority = searchDTO.getPriority();

        List<ShoppingItem> shoppingItemList = new ArrayList<>();

        if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(priority)) {
            // 只有輸入名稱
            shoppingItemList = shoppingService.searchByName(keyword);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(priority)) {
            // 有輸入名稱 + 只有主種類
            shoppingItemList = shoppingService.searchByNameAndMainCategory(keyword, mainCategory);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(priority)) {
            // 只有主種類
            shoppingItemList = shoppingService.searchByMainCategory(mainCategory);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(priority)) {
            // 有輸入名稱 + 有主種類 + 有副種類
            shoppingItemList = shoppingService.searchByNameAndMainCategoryAndSubCategory(keyword, mainCategory, subCategory);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(priority)) {
            // 有主種類 + 有副種類
            shoppingItemList = shoppingService.searchByMainCategoryAndSubCategory(mainCategory, subCategory);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(priority)) {
            // 只有輸入名稱 + 優先級
            shoppingItemList = shoppingService.searchByNameAndPriority(keyword, priority);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(priority)) {
            // 有輸入名稱 + 只有主種類 + 優先級
            shoppingItemList = shoppingService.searchByNameAndMainCategoryAndPriority(keyword, mainCategory, priority);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(priority)) {
            // 只有主種類 + 優先級
            shoppingItemList = shoppingService.searchByMainCategoryAndPriority(mainCategory, priority);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(priority)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 優先級
            shoppingItemList = shoppingService.searchByNameAndMainCategoryAndSubCategoryAndPriority(keyword, mainCategory, subCategory, priority);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(priority)) {
            // 有主種類 + 有副種類 + 優先級
            shoppingItemList = shoppingService.searchByMainCategoryAndSubCategoryAndPriority(mainCategory, subCategory, priority);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(priority)) {
            // 只有優先級
            shoppingItemList = shoppingService.searchByPriority(priority);
        } else {
            // 全空白搜尋全部
            shoppingItemList = shoppingService.searchByName("");
        }

        final List<ShoppingItemDTO> shoppingItemDTOList = shoppingItemList.stream()
                .filter(s -> Boolean.FALSE.equals(s.isPurchased())) // 只保留尚未回購的品項
                .sorted(Comparator.comparing(ShoppingItem::getPriorityType)
                        .thenComparing(Comparator.comparing(ShoppingItem::getCreationDate).reversed()))
                .map(this::constructShoppingDTO)
                .toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(shoppingItemDTOList);
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 刪除回購品項
     *
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping("/deleteShoppingItem/{id}")
    public ResponseEntity<?> deleteShoppingItem(@PathVariable String id, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[deleteShoppingItem]");
        }
        ResultStatus resultStatus = new ResultStatus();

        Optional<ShoppingItem> opt = shoppingService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C012");
            resultStatus.setMessage("查無回購物品");
        } else {
            final ShoppingItem shoppingItem = opt.get();
            shoppingItem.setDeletionDate(sdf.format(new Date()));
            shoppingItem.setModifier(member.getName());
            ShoppingItem savedShoppingItem = shoppingService.save(shoppingItem);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(constructShoppingDTO(savedShoppingItem));
            recordService.deleteShoppingItem(savedShoppingItem);
        }
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 變更購買狀態
     *
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/changePurchase/{id}")
    public ResponseEntity<?> changePurchase(@PathVariable String id, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[changePurchase]");
        }
        ResultStatus resultStatus = new ResultStatus();

        Optional<ShoppingItem> opt = shoppingService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C012");
            resultStatus.setMessage("查無回購物品");
        } else {
            final ShoppingItem shoppingItem = opt.get();
            shoppingItem.setPurchased(true);
            shoppingItem.setModifier(member.getName());
            shoppingItem.setModificationDate(sdf.format(new Date()));
            ShoppingItem savedShoppingItem = shoppingService.save(shoppingItem);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(constructShoppingDTO(savedShoppingItem));
            recordService.changePurchase(savedShoppingItem);
        }
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 變更回購品項的數量
     *
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/editPurchaseQuantity")
    public ResponseEntity<?> editPurchaseQuantity(@RequestBody PurchaseRequestDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[editPurchaseQuantity]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final Optional<ShoppingItem> opt = shoppingService.findById(dto.getId());
        if (opt.isEmpty()) {
            resultStatus.setCode("C012");
            resultStatus.setMessage("查無回購物品");
        } else {
            final ShoppingItem shoppingItem = opt.get();
            shoppingItem.setPurchaseQuantity(dto.getAmount());
            shoppingItem.setModifier(member.getName());
            shoppingItem.setModificationDate(sdf.format(new Date()));
            final ShoppingItem savedShoppingItem = shoppingService.save(shoppingItem);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(constructShoppingDTO(savedShoppingItem));
            recordService.editPurchaseQuantity(savedShoppingItem);
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 變更優先級別
     *
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/changePriority/{id}")
    public ResponseEntity<?> changePriority(@PathVariable String id, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[changePriority]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final Optional<ShoppingItem> opt = shoppingService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C012");
            resultStatus.setMessage("查無回購物品");
        } else {
            final ShoppingItem shoppingItem = opt.get();
            if (PriorityType.URGENT.equals(shoppingItem.getPriorityType())) {
                shoppingItem.setPriorityType(PriorityType.NORMAL);
            } else if (PriorityType.NORMAL.equals(shoppingItem.getPriorityType())) {
                shoppingItem.setPriorityType(PriorityType.URGENT);
            }
            shoppingItem.setModifier(member.getName());
            shoppingItem.setModificationDate(sdf.format(new Date()));
            final ShoppingItem savedShoppingItem = shoppingService.save(shoppingItem);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(constructShoppingDTO(savedShoppingItem));
            recordService.changePriority(savedShoppingItem);
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 取得本日新增回購品項的數量
     *
     * @param session
     * @return
     */
    @GetMapping("/getShoppingListSize")
    public ResponseEntity<?> getShoppingListSize(HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[getShoppingListSize]");
        }
        ResultStatus resultStatus = new ResultStatus();

        long size = shoppingService.countByDeletionDateIsNullAndPurchasedIsFalse();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(size);
        return ResponseEntity.ok(resultStatus);
    }


    public ShoppingItemDTO constructShoppingDTO(ShoppingItem shoppingItem) {
        ShoppingItemDTO dto = new ShoppingItemDTO();

        dto.setId(shoppingItem.getId());
        dto.setName(shoppingItem.getName());
        dto.setCovePath(configProperties.getPicShowPath() + shoppingItem.getCoverPath());
        dto.setSupplier(shoppingItem.getSupplier());
        dto.setPurchaseQuantity(shoppingItem.getPurchaseQuantity());

        BigDecimal purchaseQuantity = new BigDecimal(shoppingItem.getPurchaseQuantity());
        BigDecimal totalAmount = PackageForm.COMPLETE.equals(shoppingItem.getPackageForm())
                ? purchaseQuantity.multiply(new BigDecimal(shoppingItem.getPackageQuantity()))
                : purchaseQuantity;
        dto.setTotalAmount(totalAmount.toPlainString());

        dto.setDemo(shoppingItem.getDemo());
        dto.setPriorityType(shoppingItem.getPriorityType());
        dto.setPriorityTypeLabel(shoppingItem.getPriorityType() == null ? null : shoppingItem.getPriorityType().getLabel());
        dto.setMemberId(shoppingItem.getMemberId());
        dto.setPrice(shoppingItem.getPrice());
        dto.setMainCategory(shoppingItem.getMainCategory());
        dto.setMainCategoryLabel(shoppingItem.getMainCategory() == null ? null : shoppingItem.getMainCategory().getLabel());
        dto.setSubCategory(shoppingItem.getSubCategory());
        dto.setSubCategoryLabel(shoppingItem.getSubCategory() == null ? null : shoppingItem.getSubCategory().getLabel());
        dto.setPackageForm(shoppingItem.getPackageForm());
        dto.setPackageFormLabel(shoppingItem.getPackageForm() == null ? null : shoppingItem.getPackageForm().getLabel());
        dto.setPackageUnit(shoppingItem.getPackageUnit());
        dto.setPackageUnitLabel(shoppingItem.getPackageUnit() == null ? null : shoppingItem.getPackageUnit().getLabel());
        dto.setPackageQuantity(shoppingItem.getPackageQuantity());
        dto.setSupplierName(shoppingItem.getSupplier() == null ? null : shoppingItem.getSupplier().getName());
        dto.setPurchased(shoppingItem.isPurchased());
        dto.setCreationDate(shoppingItem.getCreationDate());
        dto.setModificationDate(shoppingItem.getModificationDate());
        dto.setDeletionDate(shoppingItem.getDeletionDate());
        dto.setCreator(shoppingItem.getCreator());
        dto.setModifier(shoppingItem.getModifier());

        return dto;
    }


    private InventoryDTO constructInventoryDTO(Inventory inventory, Product product, String totalAmount) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setName(product.getName());
        dto.setSerialNumber(inventory.getSerialNumber());
        dto.setReceiveFormId(inventory.getReceiveFormId());
        dto.setReceiveFormNumber(inventory.getReceiveFormNumber());
        dto.setReceiveItemId(inventory.getReceiveItemId());
        dto.setUsedDate(inventory.getUsedDate());
        dto.setStoreDate(inventory.getStoreDate());
        dto.setStoreNumber(inventory.getStoreNumber());
        dto.setExpiryDate(inventory.getExpiryDate());
        dto.setStorePlace(inventory.getStorePlace());
        dto.setStorePlaceLabel(inventory.getStorePlace() != null ? inventory.getStorePlace().getLabel() : null);
        dto.setPackageForm(inventory.getPackageForm());
        dto.setPackageFormLabel(inventory.getPackageForm() != null ? inventory.getPackageForm().getLabel() : null);
        dto.setPackageUnit(inventory.getPackageUnit());
        dto.setPackageUnitLabel(inventory.getPackageUnit() != null ? inventory.getPackageUnit().getLabel() : null);
        dto.setPackageQuantity(inventory.getPackageQuantity());
        dto.setPackageNumber(inventory.getPackageNumber());
        dto.setMainCategory(product.getMainCategory());
        dto.setSubCategory(product.getSubCategory());
        dto.setMainCategoryLabel(product.getMainCategory() != null ? product.getMainCategory().getLabel() : null);
        dto.setSubCategoryLabel(product.getSubCategory() != null ? product.getSubCategory().getLabel() : null);
        dto.setOverdueNotice(inventory.getOverdueNotice());
        dto.setSerialNumber(product.getSerialNumber());
        dto.setExpiryTime(genDateFormatted(sdf.format(new Date()), inventory.getExpiryDate()));
        dto.setExistedTime(genDateFormatted(inventory.getStoreDate(), sdf.format(new Date())));
        dto.setNoticeDate(genNoticeDateFormatted(inventory.getExpiryDate(), inventory.getOverdueNotice()));
        dto.setTotalAmount(totalAmount);
        dto.setCoverPath(configProperties.getPicShowPath() + product.getCoverName());
        dto.setAddShoppingList(product.isAddShoppingList());
        dto.setCreationDate(inventory.getCreationDate());
        dto.setModificationDate(inventory.getModificationDate());
        dto.setDeletionDate(inventory.getDeletionDate());
        dto.setCreator(inventory.getCreator());
        dto.setModifier(inventory.getModifier());
        dto.setProductId(inventory.getProductId());
        return dto;
    }
}
