package com.yfckevin.bingBao.controller;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.ConfigurationUtil;
import com.yfckevin.bingBao.dto.InventoryDTO;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.service.InventoryService;
import com.yfckevin.bingBao.service.ProductService;
import com.yfckevin.bingBao.utils.MailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yfckevin.bingBao.utils.DateUtil.genDateFormatted;
import static com.yfckevin.bingBao.utils.DateUtil.genNoticeDateFormatted;

@RestController
public class NotifyController {
    Logger logger = LoggerFactory.getLogger(NotifyController.class);
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;


    //    @GetMapping("/overdueNotice")
    @Scheduled(cron = "0 0 8 * * ?")
    public void overdueNotice() throws IOException {
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
                .toList();
        logger.info("今日寄送過期食材有" + finalInventoryDTOList.size() + "件");
        System.out.println("每日通知快過期的食材資訊: " + finalInventoryDTOList);

        ConfigurationUtil.Configuration();
        File file = new File(configProperties.getJsonPath() + "sendEmailAccount.js");
        TypeRef<List<String>> typeRef = new TypeRef<>() {
        };
        List<String> emailList = JsonPath.parse(file).read("$", typeRef);

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        contentBuilder.append("<tr><th>食材圖片</th><th>食材名稱</th><th>有效期限</th><th>剩餘數量</th><th>存放位置</th></tr>");

        for (InventoryDTO inventory : finalInventoryDTOList) {
            contentBuilder.append("<tr>");

            String coverPath = inventory.getCoverPath() != null ? inventory.getCoverPath() : "images/fridge002.jpg";
            contentBuilder.append("<td>")
                    .append("<img src='")
                    .append(coverPath)
                    .append("' alt='食材圖片' style='width: 75px; height: auto;'>")
                    .append("</td>");

            contentBuilder.append("<td>").append(inventory.getName()).append("</td>");
            contentBuilder.append("<td>").append(inventory.getExpiryDate()).append("</td>");
            contentBuilder.append("<td>").append(inventory.getTotalAmount()).append("</td>");
            contentBuilder.append("<td>").append(inventory.getStorePlace().getLabel()).append("</td>");
            contentBuilder.append("</tr>");
        }

        contentBuilder.append("</table>");

        String content = contentBuilder.toString();

        for (String email : emailList) {
            MailUtils.sendHtmlMail(email, "[每日通知] 快過期庫存食材" + finalInventoryDTOList.size() + "件", content);
        }
    }

    public NotifyController(InventoryService inventoryService, ProductService productService, SimpleDateFormat sdf, ConfigProperties configProperties) {
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.sdf = sdf;
        this.configProperties = configProperties;
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
        dto.setProductId(inventory.getProductId());
        return dto;
    }
}
