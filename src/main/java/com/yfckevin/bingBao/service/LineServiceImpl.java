package com.yfckevin.bingBao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.bingBao.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LineServiceImpl implements LineService{
    Logger logger = LoggerFactory.getLogger(LineServiceImpl.class);
    private final ConfigProperties configProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LineServiceImpl(ConfigProperties configProperties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.configProperties = configProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<?> autoReply(String msg, String replyToken) throws JsonProcessingException {
        logger.info("msg: {}", msg);
        final Map<String, Object> msgMap = objectMapper.readValue(msg, HashMap.class);
        List<Map<String, Object>> messages = new ArrayList<>();
        String type = (String) msgMap.get("type");

        if ("text".equalsIgnoreCase(type)) {
            String textContent = (String) msgMap.get("text");
            Map<String, Object> textMessage = new HashMap<>();
            textMessage.put("type", "text");
            textMessage.put("text", textContent);
            messages.add(textMessage);
        } else if ("sticker".equalsIgnoreCase(type)) {
            int packageId = (Integer) msgMap.get("packageId");
            int stickerId = (Integer) msgMap.get("stickerId");
            Map<String, Object> stickerMessage = new HashMap<>();
            stickerMessage.put("type", "sticker");
            stickerMessage.put("packageId", packageId);
            stickerMessage.put("stickerId", stickerId);
            messages.add(stickerMessage);
        } else if ("flex".equalsIgnoreCase(type)) {
            String altText = (String) msgMap.get("altText");
            Map<String, Object> contents = (Map<String, Object>) msgMap.get("contents");

            Map<String, Object> flexMessage = new HashMap<>();
            flexMessage.put("type", "flex");
            flexMessage.put("altText", altText);
            flexMessage.put("contents", contents);

            messages.add(flexMessage);

        } else {
            return ResponseEntity.badRequest().body("Unknown message type");
        }

        String url = "https://api.line.me/v2/bot/message/reply";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(configProperties.getChannelAccessToken());
        HashMap data = new HashMap<>();
        data.put("replyToken", replyToken);
        data.put("messages", messages);
        HttpEntity<HashMap> request = new HttpEntity<>(data, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println(response);
            }
        } catch (Exception e) {
            logger.error("發送訊息時報錯：{}", e.getMessage());
        }

        return ResponseEntity.ok("");
    }
}
