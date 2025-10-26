package com.treetment.backend.emotionRecord.service;

import com.treetment.backend.emotionRecord.dto.MessageDTO;
import com.treetment.backend.emotionRecord.dto.OpenAiRequestDTO;
import com.treetment.backend.emotionRecord.dto.OpenAiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GptService {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    public String getGptAdvice(String userContent) {
        if (userContent == null || userContent.isBlank()) return "오늘 하루도 고생 많으셨어요.";
        String prompt = "사용자가 남긴 하루 기록이야: \"" + userContent
                + "\". 이 기록을 바탕으로, 친구처럼 따뜻하고 공감하는 말투로 딱 한 문장의 조언이나 위로를 해줘.";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        MessageDTO message = new MessageDTO("user", prompt);
        OpenAiRequestDTO requestDTO = new OpenAiRequestDTO("gpt-3.5-turbo", List.of(message));

        HttpEntity<OpenAiRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);

        try {
            OpenAiResponseDTO response = restTemplate.postForObject(openAiUrl, requestEntity, OpenAiResponseDTO.class);
            if (response != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content();
            }
            return "AI가 응답을 생성하지 못했습니다.";
        } catch (Exception e) {
            System.err.println("OpenAI API 호출 중 오류 발생: " + e.getMessage());
            return "AI 서비스 호출에 실패했습니다.";
        }
    }
}
