package com.treetment.backend.imageRecord.service;

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
    @Value("${ai.image.url}")
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
            String errorMessage = e.getMessage();
            Throwable cause = e.getCause();
            
            if (errorMessage != null) {
                if (errorMessage.contains("UnknownHostException") || 
                    (errorMessage.contains("I/O error") && errorMessage.contains(aiServerUrl.replace("http://", "").split(":")[0]))) {
                    log.error("AI 서버를 찾을 수 없습니다. 호스트 이름 '{}'을(를) 해석할 수 없습니다. " +
                            "Docker 네트워크에서 서비스가 실행 중인지 확인하거나, " +
                            "로컬 환경에서는 localhost나 실제 IP 주소를 사용하세요. (서버 URL: {})", 
                            aiServerUrl.replace("http://", "").split(":")[0], aiServerUrl);
                } else if (errorMessage.contains("Connection refused") || 
                          (cause != null && cause.getClass().getSimpleName().contains("ConnectException"))) {
                    log.error("AI 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요 (서버 URL: {}): {}", aiServerUrl, errorMessage);
                } else {
                    log.error("AI 서버 호출 중 오류 발생 (서버 URL: {}, 이미지 URL: {}): {}", aiServerUrl, imageUrl, errorMessage);
                }
            } else {
                log.error("AI 서버 호출 중 알 수 없는 오류 발생 (서버 URL: {}, 이미지 URL: {})", aiServerUrl, imageUrl, e);
            }
            return Collections.emptyMap();
        }
        catch (Exception e) {
            // 그 외의 오류
            log.error("AI 응답 처리 중 알 수 없는 오류 발생 (서버 URL: {}, 이미지 URL: {})", aiServerUrl, imageUrl, e);
            return Collections.emptyMap();
        }
    }
}
