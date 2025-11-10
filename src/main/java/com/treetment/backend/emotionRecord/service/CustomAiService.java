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
            Map<String, Object> rawResponse = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (rawResponse == null || rawResponse.isEmpty()) {
                System.err.println("AI 서버로부터 빈 응답을 받았습니다.");
                return Collections.emptyMap();
            }
            
            System.out.println("AI 서버 원본 응답: " + rawResponse);
            
            // emotions 키가 있는 경우 중첩된 구조 처리
            Object emotionsObj = rawResponse.get("emotions");
            Map<String, Object> emotionsMap = null;
            
            if (emotionsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) emotionsObj;
                emotionsMap = temp;
            } else if (emotionsObj == null) {
                // emotions 키가 없으면 rawResponse 자체가 감정 데이터일 수 있음
                emotionsMap = rawResponse;
            }
            
            if (emotionsMap == null || emotionsMap.isEmpty()) {
                System.err.println("emotions 데이터를 찾을 수 없습니다.");
                return Collections.emptyMap();
            }
            
            // Object를 String으로 안전하게 변환
            Map<String, String> result = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : emotionsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                String valueStr = null;
                if (value instanceof String) {
                    valueStr = (String) value;
                } else if (value instanceof Number) {
                    // 숫자인 경우 퍼센트로 변환
                    double numValue = ((Number) value).doubleValue();
                    valueStr = String.format("%.2f%%", numValue * 100);
                } else if (value != null) {
                    // LinkedHashMap이나 다른 타입인 경우 문자열로 변환 시도
                    valueStr = String.valueOf(value);
                }
                
                if (valueStr != null && !valueStr.isBlank()) {
                    result.put(key, valueStr);
                }
            }
            
            System.out.println("AI 서버 응답 변환 결과: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Python AI 서버 호출 중 오류: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
