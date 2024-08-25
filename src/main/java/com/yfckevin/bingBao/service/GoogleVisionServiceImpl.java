package com.yfckevin.bingBao.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.controller.ProductController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleVisionServiceImpl implements GoogleVisionService{
    Logger logger = LoggerFactory.getLogger(GoogleVisionServiceImpl.class);
    private final ImageAnnotatorClient visionClient;
    private final ConfigProperties configProperties;

    public GoogleVisionServiceImpl (ConfigProperties configProperties) throws IOException {
        this.configProperties = configProperties;

        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(new FileInputStream(configProperties.jsonPath + "bingBao-vision-secret-key.json")))
                .build();

        visionClient = ImageAnnotatorClient.create(settings);
    }

    @Override
    public String extractText(List<AnnotateImageRequest> requests) {

        AnnotateImageResponse response = visionClient.batchAnnotateImages(requests).getResponsesList().get(0);

        if (response.hasError()) {
            logger.error("Google Vision發生錯誤：" + response.getError().getMessage());
            throw new RuntimeException("error");
        }

        TextAnnotation annotations = response.getFullTextAnnotation();
        final String text = annotations.getText();

        return text;
    }
}
