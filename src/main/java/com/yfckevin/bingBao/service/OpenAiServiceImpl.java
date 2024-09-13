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

    public final String prompt = "0. 如果文字內容與食材、健康保健食品、藥品無關，直接回傳「資料不符合食材」七個字。\n" +
            "1. 僅提取主要食材資訊，忽略配方中的成分、營養標示、使用方法、過敏原信息及保存方法。將資訊整理為 JSON 格式，並包含以下欄位：\n" +
            "   - name：食材名稱，請排除成分、營養標示及附加資訊，只保留食材名稱。\n" +
            "   - price：價格，若無標示價格，則以數字 0 表示。\n" +
            "   - mainCategory：食材主分類，僅接受以下值之一：\n" +
            "     - MEAT, SEAFOOD, VEGETABLES, FRUITS, GRAINS, BEVERAGES, CONDIMENTS, SNACKS, BAKERY, MEDICINE, HEALTH_FOOD, CANNED_FOOD, SPICES, OILS, SWEETS, DRIED_FOOD\n" +
            "   - subCategory：食材次分類，若主分類為 MEAT，則次分類需從以下值中選取：BEEF, PORK, CHICKEN。若主分類為 SEAFOOD，則次分類需從 FISH, SHRIMP 中選取。若無適用的次分類或次分類為空，請設為 null。\n" +
            "   - packageUnit：食材包裝形式，僅接受以下值之一：\n" +
            "     - EACH, BOX, PACK, BOTTLE, BAG, BARREL, CASE, CAN, BUNDLE, STRIP, PORTION\n" +
            "   - description：食材的描述與介紹，字數限制在 100-120 字之間。\n" +
            "   - packageQuantity：內容物數量，根據食材清單分析並填寫，僅接受阿拉伯數字。\n" +
            "2. 將每個食材分成獨立的 JSON 物件，忽略其他不相關資訊。\n" +
            "3. 確保最終輸出的格式為有效的 JSON。\n" +
            "\n" +
            "範例：\n" +
            "輸入：\n" +
            "產品特色每100公克300大卡，成分：分離大豆蛋白、大豆水解蛋白、大豆粉、葉酸、維生素B12等，適合所有族群。\n" +
            "輸出：\n" +
            "[\n" +
            "  {\n" +
            "    \"name\": \"大豆胜肽\",\n" +
            "    \"price\": 0,\n" +
            "    \"mainCategory\": \"HEALTH_FOOD\",\n" +
            "    \"subCategory\": null,\n" +
            "    \"packageUnit\": \"BOX\",\n" +
            "    \"description\": \"大豆胜肽是由高品質大豆製成，具有豐富的蛋白質，適合作為日常健康補給品。\",\n" +
            "    \"packageQuantity\": 1\n" +
            "  }\n" +
            "]";


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
