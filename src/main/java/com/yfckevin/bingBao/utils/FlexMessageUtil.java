package com.yfckevin.bingBao.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.InventoryDTO;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.service.InventoryService;
import com.yfckevin.bingBao.service.ProductService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yfckevin.bingBao.utils.DateUtil.genDateFormatted;
import static com.yfckevin.bingBao.utils.DateUtil.genNoticeDateFormatted;

@Component
public class FlexMessageUtil {
    Logger logger = LoggerFactory.getLogger(FlexMessageUtil.class);
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;

    public FlexMessageUtil(InventoryService inventoryService, ProductService productService, @Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties) {
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.sdf = sdf;
        this.configProperties = configProperties;
    }

    // 組建圖文輪詢
    public Map<String, Object> assembleImageCarouselTemplate() {
        logger.info("每日寄送快過期的庫存食材");
        List<Inventory> inventoryList = inventoryService.findInventoryNoticeDateIsBeforeExpiryDate();
        final List<String> productIds = inventoryList.stream().map(Inventory::getProductId).toList();
        final Map<String, Product> productMap = productService.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<String, Long> inventoryCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.counting()
                ));
        final List<InventoryDTO> inventoryDTOList = inventoryList.stream()
                .map(inventory -> {
                    final Long amount = inventoryCountMap.get(inventory.getReceiveItemId());
                    final Product product = productMap.get(inventory.getProductId());
                    return constructInventoryDTO(inventory, product, String.valueOf(amount));
                }).toList();
        //取唯一值的receiveItemId
        final List<String> uniqueReceiveItemIds = inventoryDTOList.stream().map(InventoryDTO::getReceiveItemId).distinct().toList();
        //根據唯一的 ReceiveItemId 過濾出 Inventory
        final Map<String, InventoryDTO> inventoryDTOMap = inventoryDTOList.stream().collect(Collectors.toMap(
                InventoryDTO::getReceiveItemId,
                inventoryDTO -> inventoryDTO,
                (existing, replacement) -> existing
        ));
        //取得所有唯一的 Inventory
        final List<InventoryDTO> tempInventoryDTOList = uniqueReceiveItemIds.stream()
                .map(inventoryDTOMap::get)
                .toList();
        //相同storePlace放一起且根據有效期限做遞增排序
        final Map<StorePlace, List<InventoryDTO>> groupedByStorePlace = tempInventoryDTOList.stream().collect(Collectors.groupingBy(InventoryDTO::getStorePlace));
        final List<InventoryDTO> finalInventoryDTOList = groupedByStorePlace.values().stream()
                .flatMap(inventoryDTOS -> inventoryDTOS.stream()
                        .sorted(Comparator.comparing(InventoryDTO::getExpiryDate)))
                .filter(inventoryDTO -> !"0".equals(inventoryDTO.getOverdueNotice()))   //去掉沒有設定「通知過期天數」的品項
                .toList();

        if (finalInventoryDTOList.size() == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "今日無快過期的庫存食材");
            return result;
        }

        // Flex Message
        Map<String, Object> template = new HashMap<>();
        template.put("type", "template");
        template.put("altText", "[每日通知] 冰箱內快過期的庫存食材");

        // Carousel內容
        Map<String, Object> carousel = new HashMap<>();
        carousel.put("type", "carousel");

        // 設定columns
        List<Map<String, Object>> columns = new ArrayList<>();

        for (InventoryDTO inventoryDTO : finalInventoryDTOList) {
            Map<String, Object> column = new HashMap<>();
            column.put("thumbnailImageUrl", inventoryDTO.getCoverPath());
            column.put("imageBackgroundColor", "#FFFFFF");
            column.put("title", inventoryDTO.getName());
            if (inventoryDTO.getStorePlace() != null) {
                column.put("text", "有效期限：" + inventoryDTO.getExpiryDate() + " (" + inventoryDTO.getExpiryTime() + ")" + "\n剩餘數量：" + inventoryDTO.getTotalAmount() + "\n存放位置：" + inventoryDTO.getStorePlace().getLabel());
            } else {
                column.put("text", "有效期限：" + inventoryDTO.getExpiryDate() + " (" + inventoryDTO.getExpiryTime() + ")" + "\n剩餘數量：" + inventoryDTO.getTotalAmount() + "\n存放位置： -");
            }


            // Default action (圖片)
            Map<String, String> defaultAction = new HashMap<>();
            defaultAction.put("type", "uri");
            defaultAction.put("label", "前往首頁");
            defaultAction.put("uri", "https://gurula.cc/bingBao/dashboard.html");
            column.put("defaultAction", defaultAction);

            // Actions
            List<Map<String, Object>> actions = new ArrayList<>();

            // 前往冰箱清單的action
            Map<String, Object> viewDetailAction = new HashMap<>();
            viewDetailAction.put("type", "uri");
            viewDetailAction.put("label", "前往冰箱清單");
            viewDetailAction.put("uri", "https://gurula.cc/bingBao/dashboard.html");
            actions.add(viewDetailAction);

//            // 前往報名的action
//            Map<String, Object> signupAction = new HashMap<>();
//            signupAction.put("type", "uri");
//            signupAction.put("label", "");
//            signupAction.put("uri", "https://www.gurula.cc/bingBaoTest/dashboard.html");
//            actions.add(signupAction);

            column.put("actions", actions);
            columns.add(column);
        }

        carousel.put("columns", columns);
        template.put("template", carousel);

        logger.info(new Gson().toJson(template));

        return template;
    }


    public Map<String, Object> assembleTextTemplate() {
        logger.info("每日寄送僅剩一週就過期的庫存食材");
        List<Inventory> inventoryList = inventoryService.findInventoryWithExpiryDateInSevenDays();
        final List<String> productIds = inventoryList.stream().map(Inventory::getProductId).toList();
        final Map<String, Product> productMap = productService.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<String, Long> inventoryCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.counting()
                ));
        final List<InventoryDTO> inventoryDTOList = inventoryList.stream()
                .map(inventory -> {
                    final Long amount = inventoryCountMap.get(inventory.getReceiveItemId());
                    final Product product = productMap.get(inventory.getProductId());
                    return constructInventoryDTO(inventory, product, String.valueOf(amount));
                }).toList();
        //取唯一值的receiveItemId
        final List<String> uniqueReceiveItemIds = inventoryDTOList.stream().map(InventoryDTO::getReceiveItemId).distinct().toList();
        //根據唯一的 ReceiveItemId 過濾出 Inventory
        final Map<String, InventoryDTO> inventoryDTOMap = inventoryDTOList.stream().collect(Collectors.toMap(
                InventoryDTO::getReceiveItemId,
                inventoryDTO -> inventoryDTO,
                (existing, replacement) -> existing
        ));
        //取得所有唯一的 Inventory
        final List<InventoryDTO> tempInventoryDTOList = uniqueReceiveItemIds.stream()
                .map(inventoryDTOMap::get)
                .toList();
        //相同storePlace放一起
        final Map<StorePlace, List<InventoryDTO>> groupedByStorePlace = tempInventoryDTOList.stream().collect(Collectors.groupingBy(InventoryDTO::getStorePlace));

        logger.info(String.valueOf(groupedByStorePlace));

        if (groupedByStorePlace.size() == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "今日無即期的庫存食材");
            return result;
        }

        Map<String, Object> template = new HashMap<>();
        template.put("type", "bubble");

        Map<String, Object> body = new HashMap<>();
        body.put("type", "box");
        body.put("layout", "vertical");

        List<Map<String, Object>> mainBoxContents = new ArrayList<>();

        // 標題 "最後通知"
        Map<String, Object> lastNoticeText = new HashMap<>();
        lastNoticeText.put("type", "text");
        lastNoticeText.put("text", "最後1週通知");
        lastNoticeText.put("weight", "bold");
        lastNoticeText.put("color", "#1DB446");
        lastNoticeText.put("size", "sm");
        mainBoxContents.add(lastNoticeText);

        // 標題 "即期食材"
        Map<String, Object> expiringFoodText = new HashMap<>();
        expiringFoodText.put("type", "text");
        expiringFoodText.put("text", "即期食材");
        expiringFoodText.put("weight", "bold");
        expiringFoodText.put("size", "xxl");
        expiringFoodText.put("margin", "md");
        mainBoxContents.add(expiringFoodText);

        // 有效期限
        Map<String, Object> expiryDateText = new HashMap<>();
        expiryDateText.put("type", "text");
        expiryDateText.put("text", "有效期限：" + LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        expiryDateText.put("size", "xs");
        expiryDateText.put("color", "#aaaaaa");
        expiryDateText.put("wrap", true);
        mainBoxContents.add(expiryDateText);

        // 新增分隔線
        Map<String, Object> dateSeparator = new HashMap<>();
        dateSeparator.put("type", "separator");
        dateSeparator.put("margin", "md");
        mainBoxContents.add(dateSeparator);

        // 品項和剩餘數量的標題
        Map<String, Object> headerBox = new HashMap<>();
        headerBox.put("type", "box");
        headerBox.put("layout", "horizontal");

        Map<String, Object> nameHeaderText = new HashMap<>();
        nameHeaderText.put("type", "text");
        nameHeaderText.put("text", "品項");
        nameHeaderText.put("size", "sm");
        nameHeaderText.put("color", "#111111");
        nameHeaderText.put("flex", 0);

        Map<String, Object> amountHeaderText = new HashMap<>();
        amountHeaderText.put("type", "text");
        amountHeaderText.put("text", "剩餘數量");
        amountHeaderText.put("size", "sm");
        amountHeaderText.put("color", "#111111");
        amountHeaderText.put("align", "end");

        headerBox.put("contents", Arrays.asList(nameHeaderText, amountHeaderText));
        mainBoxContents.add(headerBox);

        // 再新增一個分隔線
        Map<String, Object> separator = new HashMap<>();
        separator.put("type", "separator");
        separator.put("margin", "md");
        mainBoxContents.add(separator);

        // 每個 "冰箱" 和食材的內容
        for (Map.Entry<StorePlace, List<InventoryDTO>> entry : groupedByStorePlace.entrySet()) {
            StorePlace storePlace = entry.getKey();
            List<InventoryDTO> finalInventoryList = entry.getValue();

            // 冰箱標題
            Map<String, Object> fridgeTitleBox = new HashMap<>();
            fridgeTitleBox.put("type", "box");
            fridgeTitleBox.put("layout", "vertical");
            fridgeTitleBox.put("margin", "md");

            Map<String, Object> fridgeTitleText = new HashMap<>();
            fridgeTitleText.put("type", "text");
            fridgeTitleText.put("text", storePlace.getLabel()); // 冰箱名稱
            fridgeTitleText.put("flex", 0);
            fridgeTitleText.put("size", "md");
            fridgeTitleText.put("color", "#111111");
            fridgeTitleText.put("align", "center");

            fridgeTitleBox.put("contents", Collections.singletonList(fridgeTitleText));
            mainBoxContents.add(fridgeTitleBox);

            // 食材資訊
            for (InventoryDTO inventoryDTO : finalInventoryList) {
                Map<String, Object> foodBox = new HashMap<>();
                foodBox.put("type", "box");
                foodBox.put("layout", "horizontal");

                Map<String, Object> foodNameText = new HashMap<>();
                foodNameText.put("type", "text");
                foodNameText.put("text", inventoryDTO.getName());
                foodNameText.put("size", "sm");
                foodNameText.put("color", "#555555");
                foodNameText.put("flex", 0);
                foodNameText.put("margin", "sm");
                foodNameText.put("wrap", true);
                foodNameText.put("decoration", "underline");

                Map<String, Object> foodAmountText = new HashMap<>();
                foodAmountText.put("type", "text");
                foodAmountText.put("text", String.valueOf(inventoryDTO.getTotalAmount()));
                foodAmountText.put("size", "sm");
                foodAmountText.put("color", "#111111");
                foodAmountText.put("align", "end");

                foodBox.put("contents", Arrays.asList(foodNameText, foodAmountText));
                mainBoxContents.add(foodBox);
            }

            Map<String, Object> innerSeparator = new HashMap<>();
            innerSeparator.put("type", "separator");
            innerSeparator.put("margin", "xxl");
            mainBoxContents.add(innerSeparator);
        }

        body.put("contents", mainBoxContents);

        template.put("body", body);

        Map<String, Object> styles = new HashMap<>();
        Map<String, Object> footerStyle = new HashMap<>();
        footerStyle.put("separator", true);
        styles.put("footer", footerStyle);
        template.put("styles", styles);

        // 添加 altText
        Map<String, Object> flexMessage = new HashMap<>();
        flexMessage.put("type", "flex");
        flexMessage.put("altText", "即期食材最後一週通知");
        flexMessage.put("contents", template);

        logger.info(new Gson().toJson(flexMessage));

        return flexMessage;
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