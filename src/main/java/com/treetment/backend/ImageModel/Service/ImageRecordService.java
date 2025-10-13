package com.treetment.backend.ImageModel.Service;

import com.treetment.backend.ImageModel.DTO.ImageRecordCreateRequestDTO;
import com.treetment.backend.ImageModel.DTO.ImageRecordDetailDTO;
import com.treetment.backend.ImageModel.Repository.ImageRecordRepository;
import com.treetment.backend.ImageModel.Service.GPTService;
import com.treetment.backend.entity.EmotionRecord;
import com.treetment.backend.entity.User;
import com.treetment.backend.TextModel.UserRepository; // 공용 UserRepository 사용
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageRecordService {
    private final ImageRecordRepository imageRecordRepository;
    private final UserRepository userRepository; // 공용 UserRepository
    private final ImageAiService imageAiService;
    private final GPTService gptService;

    private record AiAnalysisResponse(String emotion, float score) {}

    @Transactional
    public ImageRecordDetailDTO createImageRecord(Long userId, ImageRecordCreateRequestDTO requestDTO) {
        // 오늘 날짜의 기록이 이미 있는지 확인합니다.
        findTodaysRecord(userId).ifPresent(record -> {
            // 기록이 이미 존재하면, 예외를 발생시킵니다.
            throw new IllegalStateException("이미 오늘 감정 기록을 작성했습니다. 기록은 하루에 한 번만 가능합니다.");
        });
        // 사용자 조회
        User user = findUserById(userId);

        // 이미지 분석 및 점수 획득
        AiAnalysisResponse analysisResponse = analyzeImageAndGetResponse(requestDTO.getEmotionImage());

        // GPT 한 줄 답변 생성
        String gptAnswer = getGptAdviceForEmotion(analysisResponse.emotion());

        // 새로운 기록 생성
        EmotionRecord newRecord = EmotionRecord.builder()
                .user(user)
                .emotionTitle(requestDTO.getEmotionTitle())
                .emotionContent(requestDTO.getEmotionContent())
                .emotionScore(analysisResponse.score())
                .emotionImage(requestDTO.getEmotionImage())
                .gptAnswer(gptAnswer)
                .build();

        EmotionRecord savedRecord = imageRecordRepository.save(newRecord);
        return ImageRecordDetailDTO.from(savedRecord);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 사용자를 찾을 수 없습니다: " + userId));
    }

    private AiAnalysisResponse analyzeImageAndGetResponse(String imageUrl) {
        Map<String, String> aiResponseMap = imageAiService.analyzeImage(imageUrl);
        if (aiResponseMap == null || aiResponseMap.isEmpty()) {
            throw new RuntimeException(
                    "AI 서버로부터 비어있는 응답을 수신했습니다.");
        }

        String emotion = aiResponseMap.get("emotion");
        String scoreStr = aiResponseMap.get("score");

        if (emotion == null || scoreStr == null) {
            throw new RuntimeException(
                    "AI 서버 응답에 필드가 누락되었습니다.");
        }

        try {
            float score = Float.parseFloat(scoreStr);
            return new AiAnalysisResponse(emotion, score);
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "AI 서버로부터 받은 점수 형식이 잘못되었습니다: " + scoreStr);
        }
    }

    private String getGptAdviceForEmotion(String detectedEmotion) {
        String prompt = String.format(
                "사용자가 업로드한 이미지에서 감지된 주된 감정은 '%s'입니다. " +
                        "이 감정과 글의 내용을 바탕으로 따뜻한 위로와 격려의 메시지를 한 문장으로 작성해 주세요.",
                 detectedEmotion
        );
        return gptService.getGptAdvice(prompt);
    }

    private Optional<EmotionRecord> findTodaysRecord(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return imageRecordRepository.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay);
    }
}
