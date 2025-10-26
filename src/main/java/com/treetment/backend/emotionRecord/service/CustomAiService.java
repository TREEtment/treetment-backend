package com.treetment.backend.emotionRecord.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomAiService {
    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private record AiRequest(String text) {}

    public Map<String, String> predictEmotion(String text) {
        String url = aiServerUrl + "/textmodel";
        AiRequest requestBody = new AiRequest(text);
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.postForObject(url, requestBody, Map.class);
            return response;
        } catch (Exception e) {
            System.err.println("Python AI 서버 호출 중 오류: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
