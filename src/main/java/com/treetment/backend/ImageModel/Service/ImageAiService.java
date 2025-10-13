package com.treetment.backend.ImageModel.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ImageAiService {
    private final RestTemplate restTemplate;

    // AI 서버 주소를 설정
    @Value("${imageai.api.url}")
    private String aiServerUrl;

    // AI 서버로 보낼 요청 DTO
    private record AiRequest(String imageUrl) {
    }

    /**
     * @param imageUrl imageUrl 분석할 이미지의 URL
     * @return 감정별 확률이 담긴 Map
     */
    public Map<String, String> analyzeImage(String imageUrl) {
        String url = aiServerUrl + "/imagemodel"; // AI 서버의 이미지 분석 엔드포인트
        AiRequest requestBody = new AiRequest(imageUrl);
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.postForObject(url, requestBody, Map.class);
            return response;
        } catch (Exception e) {
            System.err.println("Python AI 서버 호출 중 오류: " + e.getMessage());
            return Collections.emptyMap(); // 오류 시, 빈 맵 반환
        }
    }
}
