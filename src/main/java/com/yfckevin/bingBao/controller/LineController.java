package com.yfckevin.bingBao.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.Follower;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.FollowerService;
import com.yfckevin.bingBao.service.InventoryService;
import com.yfckevin.bingBao.service.LineService;
import com.yfckevin.bingBao.service.ProductService;
import com.yfckevin.bingBao.utils.FlexMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yfckevin.bingBao.utils.DateUtil.genDateFormatted;
import static com.yfckevin.bingBao.utils.DateUtil.genNoticeDateFormatted;

@RestController
public class LineController {
    private final ConfigProperties configProperties;
    private final RestTemplate restTemplate;
    private final FollowerService followerService;
    private final FlexMessageUtil flexMessageUtil;
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat ssf;
    private final LineService lineService;
    private final InventoryService inventoryService;
    private final ProductService productService;
    Logger logger = LoggerFactory.getLogger(LineController.class);

    public LineController(ConfigProperties configProperties, RestTemplate restTemplate, FollowerService followerService, FlexMessageUtil flexMessageUtil, SimpleDateFormat sdf, SimpleDateFormat ssf, LineService lineService, InventoryService inventoryService, ProductService productService) {
        this.configProperties = configProperties;
        this.restTemplate = restTemplate;
        this.followerService = followerService;
        this.flexMessageUtil = flexMessageUtil;
        this.sdf = sdf;
        this.ssf = ssf;
        this.lineService = lineService;
        this.inventoryService = inventoryService;
        this.productService = productService;
    }


    @Scheduled(cron = "0 0 8 * * ?")
    public void sendOverdueNoticeByLine() {
        try {
            final List<InventoryDTO> finalInventoryDTOList = getInventoryDTOList();

            //今日無快過期的庫存食材則不傳送任何資訊
            if (finalInventoryDTOList.size() == 0) {
                return;
            }

            int totalSize = finalInventoryDTOList.size();
            int chunkSize = 10;
            for (int i = 0; i < totalSize; i += chunkSize) {
                int index = Math.min(i + chunkSize, totalSize);
                final Map<String, Object> imageCarouselTemplate = flexMessageUtil.assembleImageCarouselTemplate(finalInventoryDTOList.subList(i, index));
                multicast(imageCarouselTemplate);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void multicast(Map<String, Object> imageCarouselTemplate) {
        String url = "https://api.line.me/v2/bot/message/multicast";
        Map<String, Object> data = new HashMap<>();
        final List<String> followerIdList = followerService.findAll().stream().map(Follower::getUserId).toList();
        data.put("to", followerIdList);
        data.put("messages", List.of(imageCarouselTemplate));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(configProperties.getChannelAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<LineUserProfileResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, entity, LineUserProfileResponseDTO.class);
    }

    private List<InventoryDTO> getInventoryDTOList() {
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
//                .filter(inventoryDTO -> !"0".equals(inventoryDTO.getOverdueNotice()))   //去掉沒有設定「通知過期天數」的品項
                .toList();
        return finalInventoryDTOList;
    }


    @Scheduled(cron = "0 0 8 * * ?")
    public void nearExpiryProductNoticeByLine() {
        try {
            final Map<String, Object> textTemplate = flexMessageUtil.assembleTextTemplate();
            //今日無即期的庫存食材則不傳送任何資訊
            if ("今日無即期的庫存食材".equals(textTemplate.getOrDefault("error", null))) {
                return;
            }
            multicast(textTemplate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    @GetMapping("/nearExpiryProductNoticeByLine")
    public ResponseEntity<?> nearExpiryProductNoticeByLine_test() {
        ResultStatus resultStatus = new ResultStatus();
        try {
            final Map<String, Object> textTemplate = flexMessageUtil.assembleTextTemplate();
            //今日無即期的庫存食材則不傳送任何資訊
            if ("今日無即期的庫存食材".equals(textTemplate.getOrDefault("error", null))) {
                resultStatus.setCode("C997");
                resultStatus.setMessage("今日無即期的庫存食材");
                return ResponseEntity.ok(resultStatus);
            }
            multicast(textTemplate);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生");
        }
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/sendOverdueNoticeByLine")
    public ResponseEntity<?> sendOverdueNoticeByLine_test() {
        ResultStatus resultStatus = new ResultStatus();
        try {
            final List<InventoryDTO> finalInventoryDTOList = getInventoryDTOList();

            //今日無快過期的庫存食材則不傳送任何資訊
            if (finalInventoryDTOList.size() == 0) {
                return ResponseEntity.ok(resultStatus);
            }

            int totalSize = finalInventoryDTOList.size();
            int chunkSize = 10;
            for (int i = 0; i < totalSize; i += chunkSize) {
                int index = Math.min(i + chunkSize, totalSize);
                final Map<String, Object> imageCarouselTemplate = flexMessageUtil.assembleImageCarouselTemplate(finalInventoryDTOList.subList(i, index));
                multicast(imageCarouselTemplate);
            }
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生");
        }

        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody LineWebhookRequestDTO dto) throws JsonProcessingException, ParseException {

        logger.info("[lines取得使用者的訊息]");

        ResultStatus resultStatus = new ResultStatus();

        String msg = "{\n" +
                "  \"type\": \"text\",\n" +
                "  \"text\": \"感謝您的訊息！\\n本系統為自動回覆功能，\\n稍後盡快回覆您訊息！\"\n" +
                "}";
        // 訊息存入redis
        Map<String, String> userData = new HashMap<>();
        for (LineWebhookRequestDTO.Event event : dto.getEvents()) {
            final String userId = event.getSource().getUserId();
            logger.info("事件類型：{}, 發送者：{}", event.getType(), userId);

            userData.put("channelID", dto.getDestination());
            userData.put("eventType", String.valueOf(event.getType()));
            if (event.getMessage() != null) {
                switch (event.getMessage().getType()) {
                    case text -> {
                        userData.put("messageText", event.getMessage().getText());
                        userData.put("messageType", String.valueOf(event.getMessage().getType()));
                        userData.put("messageId", event.getMessage().getId());
                        userData.put("messageQuoteToken", event.getMessage().getQuoteToken());
                    }
                    case audio, video -> userData.put("duration", String.valueOf(event.getMessage().getDuration()));
                    case sticker -> {
                        userData.put("stickerId", event.getMessage().getStickerId());
                        userData.put("packageId", event.getMessage().getPackageId());
                        userData.put("stickerResourceType", String.valueOf(event.getMessage().getStickerResourceType()));
                    }
                    case location -> {
                        userData.put("latitude", String.valueOf(event.getMessage().getLatitude()));
                        userData.put("longitude", String.valueOf(event.getMessage().getLongitude()));
                        userData.put("address", event.getMessage().getAddress());
                    }
                }
            }

            userData.put("redelivery", String.valueOf(event.getDeliveryContext().isRedelivery()));
            userData.put("sourceType", event.getSource().getType());
            userData.put("sourceUserId", userId);
            userData.put("webhookEventId", event.getWebhookEventId());
            userData.put("timestamp", String.valueOf(event.getTimestamp()));

            switch (event.getType()) {
                case follow -> {
                    logger.info("[follow]");
                    //取得該會員的基本資料
                    final LineUserProfileResponseDTO userProfileDTO = getUserProfile(userId);
                    Optional<Follower> followerOpt = followerService.findByUserId(userProfileDTO.getUserId());
                    Follower follower = null;
                    if (followerOpt.isPresent()) {  //追蹤者存在
                        follower = followerOpt.get();
                        follower.setFollowTime(sdf.format(new Date()));
                        follower.setUnfollowTime(null);
                    } else {    //第一次追蹤
                        follower = new Follower();
                        follower.setDisplayName(userProfileDTO.getDisplayName());
                        follower.setUserId(event.getSource().getUserId());
                        follower.setPictureUrl(userProfileDTO.getPictureUrl());
                        follower.setFollowTime(sdf.format(new Date()));
                    }
                    followerService.save(follower);

                    msg = "";
                }
                case unfollow -> {
                    logger.info("[unfollow]");
                    //取得該會員的基本資料
                    final LineUserProfileResponseDTO userProfileDTO = getUserProfile(userId);
                    Optional<Follower> followerOpt = followerService.findByUserId(userProfileDTO.getUserId());
                    Follower follower = null;
                    if (followerOpt.isPresent()) {  //追蹤者存在
                        follower = followerOpt.get();
                        follower.setUnfollowTime(sdf.format(new Date()));
                        followerService.save(follower);
                    }
                }
                case postback -> {
                    logger.info("[postback]");
                    String postbackData = event.getPostback().getData();
                    logger.info("postbackData: {}", postbackData);

                    String action = null;
                    String receiveItemId = null;
                    String productName = null;
                    String memberName = null;
                    long oldAmount = 0;

                    final Optional<Follower> followerOpt = followerService.findByUserId(userId);
                    if (followerOpt.isPresent()) {
                        memberName = followerOpt.get().getDisplayName();
                    }

                    String[] params = postbackData.split("&");
                    for (String param : params) {
                        if (param.startsWith("action=")) {
                            action = param.substring("action=".length());
                            logger.info("action: " + action);
                        } else if (param.startsWith("receiveItemId=")) {
                            receiveItemId = param.substring("receiveItemId=".length());
                            logger.info("receiveItemId: " + receiveItemId);
                        } else if (param.startsWith("productName=")) {
                            productName = param.substring("productName=".length());
                            logger.info("productName: " + productName);
                        } else if (param.startsWith("memberName=")) {
                            memberName = param.substring("memberName=".length());
                            logger.info("memberName: " + memberName);
                        } else if (param.startsWith("oldAmount=")) {
                            oldAmount = Long.parseLong(param.substring("oldAmount=".length()));
                            logger.info("oldAmount: " + oldAmount);
                        }
                    }

                    if (StringUtils.isNotBlank(action) && StringUtils.isNotBlank(receiveItemId)) {
                        switch (action) {
                            case "editExpiryDate": {
                                logger.info("重新選擇的有效期限是：" + event.getPostback().getParams().get("date"));
                                String newExpiryDate = event.getPostback().getParams().get("date");

                                oldAmount = inventoryService.findByReceiveItemId(receiveItemId).stream()
                                        .filter(inventory -> StringUtils.isBlank(inventory.getUsedDate()) &&
                                                StringUtils.isBlank(inventory.getDeletionDate()) &&
                                                !LocalDate.now().isAfter(LocalDate.parse(inventory.getExpiryDate())))
                                        .count();

                                if (oldAmount > 0) {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.setContentType(MediaType.APPLICATION_JSON);
                                    headers.set("Internal-Request", "true");
                                    ExpiryDateRequestDTO expiryDateRequestDTO = new ExpiryDateRequestDTO();
                                    expiryDateRequestDTO.setExpiryDate(newExpiryDate);
                                    expiryDateRequestDTO.setReceiveItemId(receiveItemId);
                                    expiryDateRequestDTO.setMemberName(memberName);
                                    ResponseEntity<ResultStatus> response = restTemplate.exchange(
                                            configProperties.getGlobalDomain() + "editExpiryDate",
                                            HttpMethod.POST,
                                            new HttpEntity<>(expiryDateRequestDTO, headers),
                                            ResultStatus.class
                                    );
                                    if ("C000".equals(response.getBody().getCode())) {
                                        msg = String.format("{\n" +
                                                "  \"type\": \"text\",\n" +
                                                "  \"text\": \"[%s]變更成功！\\n新的有效日期是：%s\"\n" +
                                                "}", productName, newExpiryDate);
                                    } else {
                                        msg = String.format("{\n" +
                                                "  \"type\": \"text\",\n" +
                                                "  \"text\": \"[%s]變更失敗，\\n請聯繫管理員！\"\n" +
                                                "}", productName);

                                    }
                                } else {
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]無庫存，\\n該食材已經用完或刪除。\"\n" +
                                            "}", productName);
                                }
                                lineService.autoReply(msg, event.getReplyToken());
                                break;
                            }
                            case "editAmount": {
                                logger.info("[editAmount]");
                                StringBuilder builder = new StringBuilder();
                                final String url = builder.append(configProperties.getGlobalDomain())
                                        .append("checkInventory")
                                        .append("?")
                                        .append("receiveItemId=")
                                        .append(receiveItemId)
                                        .append("&productName=")
                                        .append(productName)
                                        .append("&memberName=")
                                        .append(memberName).toString();
                                msg = String.format("{\n" +
                                        "  \"type\": \"text\",\n" +
                                        "  \"text\": \"請點擊連結修改[%s]的剩餘數量：\\n%s\"\n" +
                                        "}", productName, url);
                                lineService.autoReply(msg, event.getReplyToken());
                                break;
                            }
                            case "markAsFinished": {
                                logger.info("[markAsFinished]");

                                oldAmount = inventoryService.findByReceiveItemId(receiveItemId).stream()
                                        .filter(inventory -> StringUtils.isBlank(inventory.getUsedDate()) &&
                                                StringUtils.isBlank(inventory.getDeletionDate()) &&
                                                !LocalDate.now().isAfter(LocalDate.parse(inventory.getExpiryDate())))
                                        .count();

                                if (oldAmount <= 0) {
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]無庫存，無需再標記\"\n" +
                                            "}", productName);
                                } else {
                                    msg = String.format("{\n" +
                                            "  \"type\": \"flex\",\n" +
                                            "  \"altText\": \"確認是否標記 [%s] 用完\",\n" +
                                            "  \"contents\": {\n" +
                                            "    \"type\": \"bubble\",\n" +
                                            "    \"header\": {\n" +
                                            "      \"type\": \"box\",\n" +
                                            "      \"layout\": \"vertical\",\n" +
                                            "      \"contents\": [\n" +
                                            "        {\n" +
                                            "          \"type\": \"text\",\n" +
                                            "          \"text\": \"將 [%s] 標記用完？\",\n" +
                                            "          \"weight\": \"bold\",\n" +
                                            "          \"size\": \"lg\",\n" +
                                            "          \"wrap\": true\n" +
                                            "        }\n" +
                                            "      ]\n" +
                                            "    },\n" +
                                            "    \"body\": {\n" +
                                            "      \"type\": \"box\",\n" +
                                            "      \"layout\": \"vertical\",\n" +
                                            "      \"contents\": [\n" +
                                            "        {\n" +
                                            "          \"type\": \"text\",\n" +
                                            "          \"text\": \"剩餘數量：%d\",\n" +
                                            "          \"wrap\": true\n" +
                                            "        }\n" +
                                            "      ]\n" +
                                            "    },\n" +
                                            "    \"footer\": {\n" +
                                            "      \"type\": \"box\",\n" +
                                            "      \"layout\": \"vertical\",\n" +
                                            "      \"contents\": [\n" +
                                            "        {\n" +
                                            "          \"type\": \"button\",\n" +
                                            "          \"style\": \"primary\",\n" +
                                            "          \"color\": \"#1DB446\",\n" +
                                            "          \"action\": {\n" +
                                            "            \"type\": \"postback\",\n" +
                                            "            \"label\": \"確認\",\n" +
                                            "            \"data\": \"action=confirmMarkAsFinished&receiveItemId=%s&productName=%s&memberName=%s&oldAmount=%d\"\n" +
                                            "          }\n" +
                                            "        },\n" +
                                            "        {\n" +
                                            "          \"type\": \"separator\",\n" +
                                            "          \"margin\": \"sm\"\n" +
                                            "        },\n" +
                                            "        {\n" +
                                            "          \"type\": \"button\",\n" +
                                            "          \"style\": \"primary\",\n" +
                                            "          \"color\": \"#FF0000\",\n" +
                                            "          \"action\": {\n" +
                                            "            \"type\": \"postback\",\n" +
                                            "            \"label\": \"取消\",\n" +
                                            "            \"data\": \"action=cancelMarkAsFinished&productName=%s&receiveItemId=%s\"\n" +
                                            "          }\n" +
                                            "        }\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  }\n" +
                                            "}", productName, productName, oldAmount, receiveItemId, productName, memberName, oldAmount, productName, receiveItemId);
                                }
                                break;
                            }
                            case "cancelMarkAsFinished": {
                                logger.info("[cancelMarkAsFinished]");
                                msg = String.format("{\n" +
                                        "  \"type\": \"text\",\n" +
                                        "  \"text\": \"[%s]取消標記\"\n" +
                                        "}", productName);
                                break;
                            }
                            case "confirmMarkAsFinished": {
                                logger.info("[confirmMarkAsFinished]");
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_JSON);
                                headers.set("Internal-Request", "true");

                                UseRequestDTO useRequestDTO = new UseRequestDTO();
                                useRequestDTO.setMemberName(memberName);
                                useRequestDTO.setReceiveItemId(receiveItemId);
                                useRequestDTO.setUsedAmount((int) oldAmount);

                                ResponseEntity<ResultStatus> response = restTemplate.exchange(
                                        configProperties.getGlobalDomain() + "editAmountInventory",
                                        HttpMethod.POST,
                                        new HttpEntity<>(useRequestDTO, headers),
                                        ResultStatus.class
                                );

                                if ("C000".equals(response.getBody().getCode())) {
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]變更成功，\\n已標記用完。\"\n" +
                                            "}", productName);
                                } else if ("C006".equals(response.getBody().getCode())) {
                                    //無庫存
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]無庫存，\\n無需再標記！\"\n" +
                                            "}", productName);
                                } else if ("C007".equals(response.getBody().getCode())) {
                                    //庫存不足
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]庫存不足變更失敗，\\n請聯繫管理員！\"\n" +
                                            "}", productName);
                                }
                                break;
                            }

                        }
                    } else {
                        msg = "{\n" +
                                "  \"type\": \"text\",\n" +
                                "  \"text\": \"查無食材，\\n請聯繫管理員！\"\n" +
                                "}";
                    }
                }
            }
            lineService.autoReply(msg, event.getReplyToken());
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(dto);

        return ResponseEntity.ok(resultStatus);
    }


    private LineUserProfileResponseDTO getUserProfile(String userId) {
        String url = "https://api.line.me/v2/bot/profile/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(configProperties.getChannelAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<LineUserProfileResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, LineUserProfileResponseDTO.class);

        return response.getBody();
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
