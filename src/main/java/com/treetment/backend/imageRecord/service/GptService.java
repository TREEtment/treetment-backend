package com.treetment.backend.imageRecord.service;

import com.treetment.backend.emotionRecord.dto.MessageDTO;
import com.treetment.backend.emotionRecord.dto.OpenAiRequestDTO;
import com.treetment.backend.emotionRecord.dto.OpenAiResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;

@Slf4j
@Service("imageGptService")
@RequiredArgsConstructor
public class GptService {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @Value("${openai.api.model}")
    private String openAiApiModel;

    private static final String USER = "user";
    private static final String DEFAULT_ADVICE = "오늘 하루도 고생 많으셨습니다.";
    private static final String ERROR_ADVICE = "AI가 응답을 생성하지 못했습니다.";
    private static final String FAILURE_ADVICE = "AI 서비스 호출에 실패했습니다.";

    public String getGptAdvice(String imageAnalysis) {
        log.info("GPT 서비스 호출 시작 - imageAnalysis: {}", imageAnalysis);
        
        if(imageAnalysis == null || imageAnalysis.isBlank()) {
            log.warn("imageAnalysis가 null이거나 비어있어 기본 메시지 반환");
            return DEFAULT_ADVICE;
        }
        
        MessageDTO systemMessage = new MessageDTO("system",
                "당신은 친절하고 따뜻한 감정 분석 도우미입니다. " +
                        "사용자의 얼굴 이미지에서 감지된 감정 비율을 분석하여, 가장 높은 비율의 감정과 전체적인 감정 상태를 파악하세요. " +
                        "그 감정 상태에 맞는 위로와 격려의 메시지를 제공하되, 자연스럽고 진심 어린 톤으로 작성하세요. " +
                        "이모지는 최대 1개만 사용하고, 메시지는 30자 이내로 간결하게 작성해주세요.");
        MessageDTO userMessage = new MessageDTO(USER, imageAnalysis);

        OpenAiRequestDTO requestDTO = new OpenAiRequestDTO(
                openAiApiModel,
                List.of(systemMessage, userMessage)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<OpenAiRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);

        try {
            log.info("OpenAI API 호출 시작 - URL: {}, Model: {}", openAiApiUrl, openAiApiModel);
            OpenAiResponseDTO responseDTO = restTemplate.postForObject(
                    openAiApiUrl,
                    requestEntity,
                    OpenAiResponseDTO.class
            );

            if (responseDTO != null && !responseDTO.choices().isEmpty()) {
                String advice = responseDTO.choices().getFirst().message().content().trim();
                log.info("GPT 응답 받음 - advice: {}", advice);
                String result = advice.isEmpty() ? DEFAULT_ADVICE : advice;
                log.info("최종 반환값: {}", result);
                return result;
            } else {
                log.warn("GPT 응답이 null이거나 choices가 비어있음");
                return ERROR_ADVICE;
            }
        } catch (RestClientException e) {
            log.error("OpenAI API 호출 중 에러 발생: {}", e.getMessage(), e);
            return FAILURE_ADVICE;
        } catch (Exception e) {
            log.error("예상치 못한 에러 발생: {}", e.getMessage(), e);
            return FAILURE_ADVICE;
        }
    }
}
