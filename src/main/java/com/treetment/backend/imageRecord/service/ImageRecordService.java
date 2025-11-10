package com.treetment.backend.imageRecord.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.treetment.backend.imageRecord.dto.ImageRecordCreateRequestDTO;
import com.treetment.backend.imageRecord.dto.ImageRecordDetailDTO;
import com.treetment.backend.imageRecord.repository.ImageRecordRepository;
import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository; // 공용 UserRepository 사용
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageRecordService {
    private final ImageRecordRepository imageRecordRepository;
    private final UserRepository userRepository;
    private final ImageAiService imageAiService;
    private final GptService gptService;
    private final ObjectMapper objectMapper;

    public ImageRecordService(
            ImageRecordRepository imageRecordRepository,
            UserRepository userRepository,
            ImageAiService imageAiService,
            @Qualifier("imageGptService") GptService gptService,
            ObjectMapper objectMapper) {
        this.imageRecordRepository = imageRecordRepository;
        this.userRepository = userRepository;
        this.imageAiService = imageAiService;
        this.gptService = gptService;
        this.objectMapper = objectMapper;
    }

    // AI 분석 응답을 담기 위한 내부 DTO
    private record AiAnalysisResponse(Map<String, Float> allEmotions) {}

    @Transactional
    public ImageRecordDetailDTO createImageRecord(Long userId, ImageRecordCreateRequestDTO requestDTO) {
        // 오늘 날짜의 기록이 이미 있는지 확인
        findTodayRecord(userId).ifPresent(record -> {
            // 기록이 이미 존재하면, 예외를 발생
            throw new IllegalStateException(
                    "이미 오늘 감정 기록을 작성했습니다. " +
                            "기록은 하루에 한 번만 가능합니다.");
        });

        User user = findUserById(userId); // 사용자 조회

        // 이미지 분석 및 점수 획득
        AiAnalysisResponse analysisResponse = analyzeImageAndGetResponse(requestDTO.getEmotionImage());

        // 가중치로 최종 점수 계산
        float finalScore = calculateWeightScore(analysisResponse.allEmotions());

        // GPT 한 줄 답변 생성
        String gptAnswer = getGptAdviceFromEmotions(analysisResponse.allEmotions());
        log.info("생성된 GPT 답변: {}", gptAnswer);
        if (gptAnswer == null || gptAnswer.isBlank()) {
            log.warn("GPT 답변이 null이거나 비어있습니다!");
            gptAnswer = "오늘 하루도 고생 많으셨습니다.";
        }

        // 모든 감정 데이터를 JSON 문자열로 반환
        String allEmotionsJson = convertMapToJson(analysisResponse.allEmotions());

        // 새로운 기록 생성
        EmotionRecord newRecord = EmotionRecord.builder()
                .user(user)
                .emotionTitle("오늘의 표정 기록")
                .emotionContent(allEmotionsJson) // 모든 감정 데이터를 JSON 문자열로 저장
                .emotionScore(finalScore) // 가중치로 계산된 최종 점수 저장
                .emotionImage(requestDTO.getEmotionImage())
                .gptAnswer(gptAnswer)
                .build();
        
        log.info("저장 전 - GPT 답변: {}", newRecord.getGptAnswer());

        EmotionRecord savedRecord = imageRecordRepository.save(newRecord);
        log.info("저장된 기록 ID: {}, 저장 후 GPT 답변: {}", savedRecord.getId(), savedRecord.getGptAnswer());
        
        // 저장 후 다시 조회해서 확인
        EmotionRecord retrievedRecord = imageRecordRepository.findById(savedRecord.getId())
                .orElseThrow(() -> new RuntimeException("저장된 기록을 찾을 수 없습니다."));
        log.info("재조회 후 GPT 답변: {}", retrievedRecord.getGptAnswer());
        return ImageRecordDetailDTO.from(savedRecord);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId.intValue())
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 사용자를 찾을 수 없습니다: " + userId));
    }

    private Optional<EmotionRecord> findTodayRecord(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return imageRecordRepository.findTopByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId, startOfDay, endOfDay);
    }

    private AiAnalysisResponse analyzeImageAndGetResponse(String imageUrl) {
        Map<String, Float> aiResponseMap = imageAiService.analyzeEmotionFromImage(imageUrl);
        if (aiResponseMap == null || aiResponseMap.isEmpty()) {
            log.error("AI 서버로부터 감정 분석 결과를 받지 못했습니다. 이미지 URL: {}", imageUrl);
            throw new RuntimeException(
                    "AI 서버로부터 감정 분석 결과를 받지 못했습니다. " +
                    "AI 서버가 실행 중인지 확인하거나 네트워크 연결을 확인해주세요.");
        }
        return new AiAnalysisResponse(aiResponseMap);
    }

    private float calculateWeightScore(Map<String, Float> emotions) {
        // 감정별 가중치 설정
        Map<String, Float> weights = Map.of(
                "happy", 1.0f,
                "sad", -1.0f,
                "angry", -0.75f,
                "surprise", 0.3f,
                "fear", -0.5f,
                "disgust", -0.3f,
                "neutral", 0.0f
        );

        // 가중치 기반 점수 계산
        double weightedSum = emotions.entrySet().stream()
                .mapToDouble(entry -> entry.getValue() * weights.getOrDefault(entry.getKey().toLowerCase(), 0.0f))
                .sum();
        double scoreOutOf100 = (weightedSum + 1) * 45 + 10;
        return (float) (Math.round(scoreOutOf100 * 10.0) / 10.0);
    }

    private String convertMapToJson(Map<String, Float> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e.getMessage());
            return "{}";
        }
    }

    private String getGptAdviceFromEmotions(Map<String, Float> allEmotions) {
        // 감정을 비율 순으로 정렬하여 가장 높은 감정을 강조
        String emotionsAnalysis = allEmotions.entrySet().stream()
                .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue())) // 내림차순 정렬
                .map(entry -> {
                    String emotionName = getEmotionNameInKorean(entry.getKey());
                    return String.format("%s %.1f%%", emotionName, entry.getValue() * 100);
                })
                .collect(Collectors.joining(", "));

        StringBuilder analysisBuilder = new StringBuilder();
        analysisBuilder.append("이미지에서 감지된 감정 비율 (높은 순): ").append(emotionsAnalysis);

        return gptService.getGptAdvice(analysisBuilder.toString());
    }

    private String getEmotionNameInKorean(String emotion) {
        return switch (emotion.toLowerCase()) {
            case "happy" -> "행복";
            case "sad" -> "슬픔";
            case "angry" -> "분노";
            case "surprise" -> "놀람";
            case "fear" -> "공포";
            case "disgust" -> "혐오";
            case "neutral" -> "중립";
            default -> emotion;
        };
    }
}
