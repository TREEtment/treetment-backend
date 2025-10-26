package com.treetment.backend.ImageModel.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Collections;

@Slf4j // 로그 출력을 위한 어노테이션
@Service
@RequiredArgsConstructor
public class ImageAiService {
    private final RestTemplate restTemplate;

    // AI 서버 주소를 설정
    @Value("${ai.server.url}")
    private String aiServerUrl;

    // AI 서버로 보낼 요청 DTO
    private record AiRequest(String imageUrl) {}

    // AI 서버로부터 받을 응답 DTO
    // ex) {"happy": 0.8, "sad": 0.1, "angry": 0.1}
    private record AiResponse(Map<String, Float> emotions) {}

    /**
     * @param imageUrl imageUrl 분석할 이미지의 URL
     * @return 감정별 확률(Float)이 담긴 Map
     */
    public Map<String, Float> analyzeEmotionFromImage(String imageUrl) {
        String url = aiServerUrl + "/imagemodel"; // AI 서버의 이미지 분석 엔드포인트
        AiRequest requestBody = new AiRequest(imageUrl);
        try {
            AiResponse response = restTemplate.postForObject(url, requestBody, AiResponse.class);
            if (response != null && response.emotions() != null) {
                return response.emotions();
            }

            log.warn("AI 서버로부터 비어있는 응답을 받았습니다. (URL: {})", imageUrl);
            return Collections.emptyMap();
        }
        catch (RestClientException e) {
            // 네트워크 오류나 HTTP 4xx, 5xx 에러 등
            log.error("AI 서버 호출 중 오류 발생 (URL: {}): {}", imageUrl, e.getMessage());
            return Collections.emptyMap();
        }
        catch (Exception e) {
            // 그 외의 오류
            log.error("AI 응답 처리 중 알 수 없는 오류 발생", e);
            return Collections.emptyMap();
        }
    }
}
