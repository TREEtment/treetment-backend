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

    @Value("${ai.text.url}")
    private String aiServerUrl;

    private record AiRequest(String text) {}

    public Map<String, String> predictEmotion(String text) {
        String url = aiServerUrl + "/textmodel";
        AiRequest requestBody = new AiRequest(text);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (response == null) {
                return Collections.emptyMap();
            }
            
            // Object를 String으로 안전하게 변환
            Map<String, String> result = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : response.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    result.put(entry.getKey(), (String) value);
                } else if (value instanceof Number) {
                    result.put(entry.getKey(), String.valueOf(value));
                } else {
                    // LinkedHashMap이나 다른 타입인 경우 문자열로 변환
                    result.put(entry.getKey(), String.valueOf(value));
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("Python AI 서버 호출 중 오류: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
