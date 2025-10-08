package com.treetment.backend.EmotionReport;

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
public class GptServiceReport {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    public String summarizeGptAnswers(List<String> gptAnswers) {
        if (gptAnswers == null || gptAnswers.isEmpty()) {
            return "분석할 주간 감정 요약이 없습니다.";
        }

        String combinedAnswers = String.join("\n- ", gptAnswers);
        String prompt = "다음은 사용자의 한 주간 일일 감정에 한마디 조언을 해준것이야: \n- " + combinedAnswers
                + "\n\n이 내용을 종합해서, 사용자의 주간 감정 흐름을 친구처럼 따뜻한 말투로 최종 분석하고 조언도 해줘.";

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
