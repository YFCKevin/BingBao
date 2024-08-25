package com.yfckevin.bingBao.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.yfckevin.bingBao.ConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {
    private final ConfigProperties configProperties;

    public MongoConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    protected String getDatabaseName() {
        return "bingBao";
    }

    @Override
    public MongoClient mongoClient() {
        // vm上的mongo
        return MongoClients.create(configProperties.getMongodbUri());
        // 本地測試用
//        return MongoClients.create("mongodb://localhost:27017");
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), "bingBao");
    }

}
