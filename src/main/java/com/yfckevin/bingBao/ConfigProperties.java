package com.yfckevin.bingBao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigProperties {
    @Value("${config.apiKey}")
    public String apiKey;
    @Value("${spring.security.user.name}")
    public String username;
    @Value("${spring.security.user.password}")
    public String password;
    @Value("${config.jsonPath}")
    public String jsonPath;
    @Value("${config.fileSavePath}")
    public String fileSavePath;
    @Value("${config.picSavePath}")
    public String picSavePath;
    @Value("${config.picShowPath}")
    public String picShowPath;
    @Value("${config.globalDomain}")
    public String globalDomain;
    @Value("${spring.data.mongodb.uri}")
    public String mongodbUri;
    @Value("${config.aiPicSavePath}")
    public String aiPicSavePath;

    public String getApiKey() {
        return apiKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public String getPicSavePath() {
        return picSavePath;
    }

    public String getPicShowPath() {
        return picShowPath;
    }

    public String getGlobalDomain() {
        return globalDomain;
    }

    public String getMongodbUri() {
        return mongodbUri;
    }

    public String getAiPicSavePath() {
        return aiPicSavePath;
    }
}
