package com.yfckevin.bingBao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.ChatCompletionResponse;
import com.yfckevin.bingBao.entity.TempDetail;
import com.yfckevin.bingBao.entity.TempMaster;
import com.yfckevin.bingBao.exception.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiServiceImpl implements OpenAiService{
    Logger logger = LoggerFactory.getLogger(OpenAiServiceImpl.class);
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final TempMasterService tempMasterService;

    public OpenAiServiceImpl(ConfigProperties configProperties, ObjectMapper objectMapper, RestTemplate restTemplate, TempMasterService tempMasterService) {
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.tempMasterService = tempMasterService;
    }

    public final String prompt = "0.文字內容跟食材無相關，直接回傳「資料不符合食材」7個字就好\n" +
            "1.食材清單如上，我要把資訊整理出JSON檔，欄位包括：\n" +
            "name：食材名稱\n" +
            "price：價格，以數字表示，沒有標示則為0\n" +
            "mainCategory：食材主種類，限定其中一種MEAT,SEAFOOD,VEGETABLES,FRUITS,GRAINS,BEVERAGES,CONDIMENTS,SNACKS,BAKERY,MEDICINE,HEALTH_FOOD,CANNED_FOOD,SPICES,OILS,SWEETS,DRIED_FOOD\n" +
            "subCategory：食材次種類，如果主種類是MEAT，則再從BEEF,PORK,CHICKEN選定一種。如果主種類是SEAFOOD，則再從FISH,SHRIMP選定一種。如果不是NEAT和SEAFOOD，則沒有subCategory屬性\n" +
            "packageUnit：食材包裝形式，限定其中一種EACH,BOX,PACK,BOTTLE,BAG,BARREL,CASE,CAN,BUNDLE,STRIP,PORTION\n" +
            "description：食材的介紹與描述，字數落在100-120字\n" +
            "packageQuantity：內容物數量，從食材清單分析內容物有多少，以字串表示數字\n" +
            "2.每份食材清單可能包含多個食材資訊，必須分開成獨立的JSON物件。\n" +
            "3.確保只匯出json格式";

    @Override
    public ResultStatus<TempMaster> completion(String rawText) throws JsonProcessingException {
        ResultStatus<TempMaster> resultStatus = new ResultStatus<>();
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(configProperties.getApiKey());

        String data = createPayload(rawText + "\n" + prompt);

        HttpEntity<String> entity = new HttpEntity<>(data, headers);

        ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ChatCompletionResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("OpenAI回傳的status code: {}", response);
            ChatCompletionResponse responseBody = response.getBody();
            String content = extractContent(responseBody);
            System.out.println("GPT回傳資料 ======> " + content);

            if ("資料不符合食材".equals(content)){
                logger.error("客戶提供資料不符合");
                resultStatus.setCode("C998");
                resultStatus.setMessage("客戶提供資料不符合");
                return resultStatus;
            }

            TempMaster tempMaster = new TempMaster();
            List<TempDetail> tempDetailList = objectMapper.readValue(content, new TypeReference<>() {
            });
            tempMaster.setTempDetails(tempDetailList);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(tempMaster);
        } else {
            logger.error("openAI錯誤發生");
            resultStatus.setCode("C999");
            resultStatus.setMessage("異常發生");
        }
        return resultStatus;
    }


    private String createPayload(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        payload.put("messages", new Object[]{message});

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String extractContent(ChatCompletionResponse responseBody) {
        if (responseBody != null && !responseBody.getChoices().isEmpty()) {
            ChatCompletionResponse.Choice choice = responseBody.getChoices().get(0);
            if (choice != null && choice.getMessage() != null) {
                String content = choice.getMessage().getContent().trim();

                // 去掉反引號
                if (content != null) {
                    content = content.replace("```json", "").replace("```", "").trim();
                }

                return content;
            }
        }
        return null;
    }
}
