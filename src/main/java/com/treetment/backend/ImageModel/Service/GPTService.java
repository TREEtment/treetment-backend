package com.treetment.backend.ImageModel.Service;

import com.treetment.backend.TextModel.MessageDTO;
import com.treetment.backend.TextModel.OpenAiRequestDTO;
import com.treetment.backend.TextModel.OpenAiResponseDTO;
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
public class GPTService {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    public String getGptAdvice(String prompt) {
        if(prompt == null || prompt.isEmpty()) {
            return "오늘 하루도 고생 많으셨어요!.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        MessageDTO message = new MessageDTO("user", prompt);
        OpenAiRequestDTO requestDTO = new OpenAiRequestDTO("gpt-3.5-turbo", List.of(message));

        HttpEntity<OpenAiRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);

        try {
            String openAiUrl = "https://api.openai.com/v1/chat/completions";
            OpenAiResponseDTO response = restTemplate.postForObject(openAiUrl, requestEntity, OpenAiResponseDTO.class);
            if (response != null && !response.choices().isEmpty()) {
                return response.choices().getFirst().message().content();
            }
            return "AI가 응답을 생성하지 못했습니다.";
        } catch (Exception e) {
            System.err.println("OpenAI API 호출 중 오류 발생: " + e.getMessage());
            return "AI 서비스 호출에 실패했습니다.";
        }
    }
}