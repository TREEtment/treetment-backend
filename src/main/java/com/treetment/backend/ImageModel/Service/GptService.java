package com.treetment.backend.ImageModel.Service;

import com.treetment.backend.TextModel.MessageDTO;
import com.treetment.backend.TextModel.OpenAiRequestDTO;
import com.treetment.backend.TextModel.OpenAiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Slf4j // 로그 출력을 위한 어노테이션
@Service
@RequiredArgsConstructor
public class GptService {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String openAiUrl;

    @Value("${openai.model}")
    private String model;

    private static final String USER = "user";
    private static final String DEFAULT_ADVICE = "오늘 하루도 고생 많으셨어요!";
    private static final String ERROR_ADVICE = "AI가 응답을 생성하지 못했습니다.";
    private static final String FAILURE_ADVICE = "AI 서비스 호출에 실패했습니다.";

    public String getGptAdvice(String prompt) {
        if(prompt == null || prompt.isBlank()) {
            return DEFAULT_ADVICE;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        MessageDTO message = new MessageDTO(USER, prompt);
        OpenAiRequestDTO requestDTO = new OpenAiRequestDTO(model, List.of(message));

        HttpEntity<OpenAiRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);

        try {
            OpenAiResponseDTO response = restTemplate.postForObject(openAiUrl, requestEntity, OpenAiResponseDTO.class);
            if (response != null && !response.choices().isEmpty()) {
                return response.choices().getFirst().message().content();
            }
            log.warn("OpenAI API로부터 비어있는 응답을 받았습니다.");
            return ERROR_ADVICE;
        }
        catch (RestClientException e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return FAILURE_ADVICE;
        }
    }
}