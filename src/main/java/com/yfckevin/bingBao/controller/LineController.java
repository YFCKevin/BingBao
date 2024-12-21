package com.yfckevin.bingBao.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.Follower;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.FollowerService;
import com.yfckevin.bingBao.service.InventoryService;
import com.yfckevin.bingBao.service.LineService;
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
    Logger logger = LoggerFactory.getLogger(LineController.class);

    public LineController(ConfigProperties configProperties, RestTemplate restTemplate, FollowerService followerService, FlexMessageUtil flexMessageUtil, SimpleDateFormat sdf, SimpleDateFormat ssf, LineService lineService, InventoryService inventoryService) {
        this.configProperties = configProperties;
        this.restTemplate = restTemplate;
        this.followerService = followerService;
        this.flexMessageUtil = flexMessageUtil;
        this.sdf = sdf;
        this.ssf = ssf;
        this.lineService = lineService;
        this.inventoryService = inventoryService;
    }


    @Scheduled(cron = "0 0 8 * * ?")
    public void sendOverdueNoticeByLine() {
        try {
            final Map<String, Object> imageCarouselTemplate = flexMessageUtil.assembleImageCarouselTemplate();
            //今日無快過期的庫存食材則不傳送任何資訊
            if ("今日無快過期的庫存食材".equals(imageCarouselTemplate.getOrDefault("error", null))) {
                return;
            }
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }


    @Scheduled(cron = "0 0 8 * * ?")
    public void nearExpiryProductNoticeByLine() {
        try {
            final Map<String, Object> textTemplate = flexMessageUtil.assembleTextTemplate();
            //今日無即期的庫存食材則不傳送任何資訊
            if ("今日無即期的庫存食材".equals(textTemplate.getOrDefault("error", null))) {
                return;
            }
            String url = "https://api.line.me/v2/bot/message/multicast";
            Map<String, Object> data = new HashMap<>();
            final List<String> followerIdList = followerService.findAll().stream().map(Follower::getUserId).toList();
            data.put("to", followerIdList);
            data.put("messages", List.of(textTemplate));
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(configProperties.getChannelAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            ResponseEntity<LineUserProfileResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, entity, LineUserProfileResponseDTO.class);
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
            String url = "https://api.line.me/v2/bot/message/multicast";
            Map<String, Object> data = new HashMap<>();
            final List<String> followerIdList = followerService.findAll().stream().map(Follower::getUserId).toList();
            data.put("to", followerIdList);
            data.put("messages", List.of(textTemplate));
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(configProperties.getChannelAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            ResponseEntity<LineUserProfileResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, entity, LineUserProfileResponseDTO.class);
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
            final Map<String, Object> imageCarouselTemplate = flexMessageUtil.assembleImageCarouselTemplate();
            //今日無快過期的庫存食材則不傳送任何資訊
            if ("今日無快過期的庫存食材".equals(imageCarouselTemplate.getOrDefault("error", null))) {
                resultStatus.setCode("C997");
                resultStatus.setMessage("今日無快過期的庫存食材");
                return ResponseEntity.ok(resultStatus);
            }
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
                        }
                    }

                    if (StringUtils.isNotBlank(action) && StringUtils.isNotBlank(receiveItemId)) {
                        switch (action) {
                            case "editExpiryDate": {
                                logger.info("重新選擇的有效期限是：" + event.getPostback().getParams().get("date"));
                                String newExpiryDate = event.getPostback().getParams().get("date");
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
                                    lineService.autoReply(msg, event.getReplyToken());
                                } else {
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]變更失敗，\\n請聯繫管理員！\"\n" +
                                            "}", productName);
                                    lineService.autoReply(msg, event.getReplyToken());
                                }
                                break;
                            }
                            case "editAmount": {
                                logger.info("[editAmount]");
                                final long oldAmount = inventoryService.findByReceiveItemId(receiveItemId).stream()
                                        .filter(inventory -> StringUtils.isBlank(inventory.getUsedDate()) &&
                                                StringUtils.isBlank(inventory.getDeletionDate()) &&
                                                !LocalDate.now().isAfter(LocalDate.parse(inventory.getExpiryDate())))
                                        .count();
                                StringBuilder builder = new StringBuilder();
                                final String url = builder.append(configProperties.getGlobalDomain())
                                        .append("edit-amount-page.html")
                                        .append("?")
                                        .append("receiveItemId=")
                                        .append(receiveItemId)
                                        .append("&productName=")
                                        .append(productName)
                                        .append("&oldAmount=")
                                        .append(oldAmount)
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
                                final long oldAmount = inventoryService.findByReceiveItemId(receiveItemId).stream()
                                        .filter(inventory -> StringUtils.isBlank(inventory.getUsedDate()) &&
                                                StringUtils.isBlank(inventory.getDeletionDate()) &&
                                                !LocalDate.now().isAfter(LocalDate.parse(inventory.getExpiryDate())))
                                        .count();
                                if (oldAmount > 0) {
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
                                    } else {
                                        msg = String.format("{\n" +
                                                "  \"type\": \"text\",\n" +
                                                "  \"text\": \"[%s]變更失敗，\\n請聯繫管理員！\"\n" +
                                                "}", productName);
                                    }
                                } else {
                                    msg = String.format("{\n" +
                                            "  \"type\": \"text\",\n" +
                                            "  \"text\": \"[%s]無庫存，無需標記\"\n" +
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
}
