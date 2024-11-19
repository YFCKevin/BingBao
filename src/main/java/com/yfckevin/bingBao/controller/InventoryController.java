package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.*;
import com.yfckevin.bingBao.utils.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yfckevin.bingBao.utils.DateUtil.genDateFormatted;
import static com.yfckevin.bingBao.utils.DateUtil.genNoticeDateFormatted;

@RestController
public class InventoryController {
    Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final DataProcessService dataProcessService;
    private final RestTemplate restTemplate;

    public InventoryController(@Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, InventoryService inventoryService, ProductService productService, DataProcessService dataProcessService, RestTemplate restTemplate) {
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.dataProcessService = dataProcessService;
        this.restTemplate = restTemplate;
    }

    /**
     * 使用食材對庫存數量進行扣減
     *
     * @param session
     * @return
     */
    @PostMapping("/editAmountInventory")
    public ResponseEntity<?> editAmountInventory(@RequestBody UseRequestDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[useInventoryProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Inventory> inventoryList = inventoryService.findByReceiveItemIdAndUsedDateIsNull(dto.getReceiveItemId());
        if (inventoryList.size() == 0) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
            return ResponseEntity.ok(resultStatus);
        }

        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        int amountToUse = dto.getUsedAmount();
        for (Inventory inventory : inventoryList) {
            inventory.setUsedDate(sdf.format(new Date()));
            inventory.setModifier(member.getName());
            inventory.setModificationDate(sdf.format(new Date()));
            inventoriesToUpdate.add(inventory);
            amountToUse--;

            if (amountToUse <= 0){
                break;
            }
        }
        if (!inventoriesToUpdate.isEmpty()) {
            inventoryService.saveAll(inventoriesToUpdate);
        }

        if (amountToUse > 0) {
            resultStatus.setCode("C007");
            resultStatus.setMessage("庫存不足");
        } else {
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }

        dataProcessService.inventoryDataProcess(inventoriesToUpdate);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 變更庫存食材的存放位置
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/editStorePlaceInventory")
    public ResponseEntity<?> editStorePlaceInventory(@RequestBody ChangeStorePlaceRequestDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[editStorePlaceInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Inventory> inventoryList = inventoryService.findByReceiveItemIdAndUsedDateIsNull(dto.getReceiveItemId());
        if (inventoryList.size() == 0) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
            return ResponseEntity.ok(resultStatus);
        }

        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        final String newStorePlace = dto.getNewStorePlace();
        for (Inventory inventory : inventoryList) {
            inventory.setStorePlace(StorePlace.valueOf(newStorePlace));
            inventoriesToUpdate.add(inventory);
        }
        if (!inventoriesToUpdate.isEmpty()) {
            inventoryService.saveAll(inventoriesToUpdate);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }

        dataProcessService.inventoryDataProcess(inventoriesToUpdate);

        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/deleteInventory/{id}")
    public ResponseEntity<?> deleteInventory (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[deleteInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final List<Inventory> inventoryList = inventoryService.findByReceiveItemIdAndUsedDateIsNull(id);
        final List<Inventory> inventoriesToDelete = inventoryList.stream().peek(i -> i.setDeletionDate(sdf.format(new Date()))).toList();
        inventoryService.saveAll(inventoriesToDelete);

        dataProcessService.inventoryDataProcess(inventoriesToDelete);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢單一庫存食材資訊
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/getInventoryInfo/{id}")
    public ResponseEntity<?> getInventoryInfo (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[deleteInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final Optional<Inventory> opt = inventoryService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C008");
            resultStatus.setMessage("查無庫存資料");
        } else {
            final Inventory inventory = opt.get();
            final Optional<Product> productOpt = productService.findById(inventory.getProductId());
            if (productOpt.isEmpty()) {
                resultStatus.setCode("C001");
                resultStatus.setMessage("查無食材");
            } else {
                final Product product = productOpt.get();
                final InventoryDTO inventoryDTO = constructInventoryDTO(inventory, product, null);
                resultStatus.setCode("C000");
                resultStatus.setMessage("成功");
                resultStatus.setData(inventoryDTO);
            }
        }
        return ResponseEntity.ok(resultStatus);
    }



    @PostMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestBody List<SearchDTO> searchCondition, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[dashboard]");
        }
        ResultStatus resultStatus = new ResultStatus();

        Map<String, List<InventoryDTO>> inventoryMap = new HashMap<>();

        String url = configProperties.getGlobalDomain() + "inventorySearch";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Internal-Request", "true");

        //今日放進冰箱的
        final SearchDTO todaySearchDTO = searchCondition.stream()
                .filter(searchDTO -> "today".equals(searchDTO.getType())).findFirst().orElse(new SearchDTO("today"));
        HttpEntity<SearchDTO> todayEntity = new HttpEntity<>(todaySearchDTO, headers);
        ResponseEntity<ResultStatus<List<InventoryDTO>>> todayResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                todayEntity,
                new ParameterizedTypeReference<>() {}
        );
        if ("C000".equals(todayResponse.getBody().getCode())) {
            System.out.println(123);
            inventoryMap.put("today", todayResponse.getBody().getData());
        }


        //快過期的且沒用完
        final SearchDTO soonSearchDTO = searchCondition.stream()
                .filter(searchDTO -> "soon".equals(searchDTO.getType())).findFirst().orElse(new SearchDTO("soon"));
        HttpEntity<SearchDTO> soonEntity = new HttpEntity<>(soonSearchDTO, headers);
        ResponseEntity<ResultStatus<List<InventoryDTO>>> soonResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                soonEntity,
                new ParameterizedTypeReference<>() {}
        );
        if ("C000".equals(soonResponse.getBody().getCode())) {
            inventoryMap.put("expiringSoon", soonResponse.getBody().getData());
        }


        //在有效期限內且沒用完
        final SearchDTO validSearchDTO = searchCondition.stream()
                .filter(searchDTO -> "valid".equals(searchDTO.getType())).findFirst().orElse(new SearchDTO("valid"));
        HttpEntity<SearchDTO> validEntity = new HttpEntity<>(validSearchDTO, headers);
        ResponseEntity<ResultStatus<List<InventoryDTO>>> validResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                validEntity,
                new ParameterizedTypeReference<>() {}
        );
        if ("C000".equals(validResponse.getBody().getCode())) {
            inventoryMap.put("valid", validResponse.getBody().getData());
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(inventoryMap);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/inventorySearch")
    public ResponseEntity<?> inventorySearch(@RequestBody SearchDTO searchDTO, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[inventorySearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final String keyword = searchDTO.getKeyword().trim();
        final String mainCategory = searchDTO.getMainCategory();
        final String subCategory = searchDTO.getSubCategory();
        final String type = searchDTO.getType();
        final String storePlace = searchDTO.getStorePlace();
        final String supplierId = searchDTO.getSupplierId();
        System.out.println(keyword + " / " + mainCategory + " / " + subCategory + " / " + storePlace + " / " + supplierId);
        Map<String, Map<Long, List<Inventory>>> itemIdAmountInventoryMap = new HashMap<>();
        if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 只有輸入名稱
            itemIdAmountInventoryMap = inventoryService.searchByName(keyword, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategory(keyword, mainCategory, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 只有主種類
            itemIdAmountInventoryMap = inventoryService.searchByMainCategory(mainCategory, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategory(keyword, mainCategory, subCategory, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有主種類 + 有副種類
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategory(mainCategory, subCategory, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 輸入名稱 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByNameAndStorePlace(keyword, type, storePlace);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndStorePlace(keyword, mainCategory, type, storePlace);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 主種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndStorePlace(mainCategory, type, storePlace);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategoryAndStorePlace(keyword, mainCategory, subCategory, type, storePlace);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有主種類 + 有副種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategoryAndStorePlace(mainCategory, subCategory, type, storePlace);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 只有存放位置
            itemIdAmountInventoryMap = inventoryService.searchByStorePlace(type, storePlace);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有輸入名稱 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndSupplierId(keyword, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類 + 應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSupplierId(keyword, mainCategory, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有主種類 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSupplierId(mainCategory, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategoryAndSupplierId(keyword, mainCategory, subCategory, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有主種類 + 有副種類 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategoryAndSupplierId(mainCategory, subCategory, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 輸入名稱 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndStorePlaceAndSupplierId(keyword, storePlace, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndStorePlaceAndSupplierId(keyword, mainCategory, storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 主種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndStorePlaceAndSupplierId(mainCategory, storePlace, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategoryAndStorePlaceAndSupplierId(keyword, mainCategory, subCategory, storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有主種類 + 有副種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategoryAndStorePlaceAndSupplierId(mainCategory, subCategory, storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByStorePlaceAndSupplierId(storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有供應商名
            itemIdAmountInventoryMap = inventoryService.searchBySupplierId(supplierId, type);
        } else {
            // 全空白搜尋全部
            itemIdAmountInventoryMap = inventoryService.searchByName("", type);
        }

        final Map<String, Long> receiveItemIdInventoryAmountMap = itemIdAmountInventoryMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream().flatMap(List::stream).count()
                ));
        final List<Inventory> inventoryList = itemIdAmountInventoryMap.values().stream()
                .flatMap(innerMap -> innerMap.values().stream())
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();
        final List<String> productIds = inventoryList.stream().map(Inventory::getProductId).toList();
        final Map<String, Product> tempProductMap = productService.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<InventoryDTO> finalInventory = inventoryList.stream()
                .map(inventory -> {
                    final Long totalAmount = receiveItemIdInventoryAmountMap.get(inventory.getReceiveItemId());
                    final Product product = tempProductMap.get(inventory.getProductId());
                    return constructInventoryDTO(inventory, product, String.valueOf(totalAmount));
                })
                .sorted(Comparator.comparing(InventoryDTO::getExpiryDate))
                .toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(finalInventory);
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getTodaySize")
    public ResponseEntity<?> getTodaySize(HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[getTodaySize]");
        }
        ResultStatus resultStatus = new ResultStatus();

        //今日放進冰箱的
        final int size = inventoryService.findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod().size();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(size);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/exportExcel/{type}")
    public void exportExcel (@PathVariable String type, @RequestBody List<InventoryDTO> inventoryDTOList, HttpSession session, HttpServletResponse response) throws Exception {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[exportExcel]");
        }
        FileUtils.constructExcel(inventoryDTOList, type, response, member);
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
        dto.setCreationDate(inventory.getCreationDate());
        dto.setModificationDate(inventory.getModificationDate());
        dto.setDeletionDate(inventory.getDeletionDate());
        dto.setCreator(inventory.getCreator());
        dto.setModifier(inventory.getModifier());
        return dto;
    }
}
