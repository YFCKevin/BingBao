package com.yfckevin.bingBao.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;

import java.util.List;

public interface GoogleVisionService {
    String extractText(List<AnnotateImageRequest> requests);
}
