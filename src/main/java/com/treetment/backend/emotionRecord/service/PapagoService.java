package com.treetment.backend.emotionRecord.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PapagoService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${papago.api.url}")
    private String apiUrl;
    @Value("${papago.api.key.id}")
    private String apiKeyId;
    @Value("${papago.api.key.secret}")
    private String apiKeySecret;
    public String translateToEnglish(String koreanText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", apiKeyId);
        headers.set("X-NCP-APIGW-API-KEY", apiKeySecret);

        Map<String, String> body = Map.of(
                "source", "ko",
                "target", "en",
                "text", koreanText
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            return parseTranslatedText(responseEntity.getBody());

        } catch (Exception e) {
            System.err.println("Papago API 호출 중 오류 발생: " + e.getMessage());
            return "Error during translation.";
        }
    }
    private String parseTranslatedText(String jsonResponse) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        return rootNode.path("message").path("result").path("translatedText").asText();
    }


}
