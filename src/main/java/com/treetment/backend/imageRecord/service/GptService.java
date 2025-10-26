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
        if(imageAnalysis == null || imageAnalysis.isBlank()) {
            return DEFAULT_ADVICE;
        }
        MessageDTO systemMessage = new MessageDTO("system",
                "당신은 친절한 감정 분석 도우미입니다. " +
                        "사용자가 업로드한 얼굴 이미지에 대한 감정 비율 별 분석 내용을 바탕으로 오늘 하루를 위로하고 격려하는 짧은 조언을 제공하세요. " +
                        "조언은 30자 이내로 작성해 주세요.");
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
            OpenAiResponseDTO responseDTO = restTemplate.postForObject(
                    openAiApiUrl,
                    requestEntity,
                    OpenAiResponseDTO.class
            );

            if (responseDTO != null && !responseDTO.choices().isEmpty()) {
                String advice = responseDTO.choices().getFirst().message().content().trim();
                return advice.isEmpty() ? DEFAULT_ADVICE : advice;
            } else {
                return ERROR_ADVICE;
            }
        } catch (RestClientException e) {
            log.error("OpenAI API 호출 중 에러 발생: {}", e.getMessage());
            return FAILURE_ADVICE;
        }
    }
}
