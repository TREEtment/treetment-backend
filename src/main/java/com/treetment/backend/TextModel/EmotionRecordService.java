package com.treetment.backend.TextModel;
import com.treetment.backend.entity.EmotionRecord;
import com.treetment.backend.auth.entity.User;
import com.treetment.backend.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
@Service
@RequiredArgsConstructor

public class EmotionRecordService {
    private final EmotionRecordRepository2 emotionRecordRepository2;
    private final UserRepository userRepository;
    private final CustomAiService customAiService;
    private final GptService gptService;
    private final PapagoService papagoService;

    @Transactional
    public EmotionRecordDetailDTO createRecord(Integer userId, EmotionRecordCreateRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        String originalContent = requestDTO.getEmotionContent();
        String translatedContent = papagoService.translateToEnglish(originalContent);

        Map<String, String> emotionProbabilities = customAiService.predictEmotion(translatedContent);
        float emotionScore = calculateWeightedScore(emotionProbabilities);
        String gptAnswer = gptService.getGptAdvice(requestDTO.getEmotionContent());

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        Optional<EmotionRecord> existingRecordOpt = emotionRecordRepository2
                .findTopByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay);
        EmotionRecord recordToSave;
        if (existingRecordOpt.isPresent()) {
            recordToSave = existingRecordOpt.get();
            recordToSave.update(requestDTO.getEmotionTitle(), originalContent, emotionScore, gptAnswer);
        }
        else {
            recordToSave = EmotionRecord.builder()
                    .user(user)
                    .emotionTitle(requestDTO.getEmotionTitle())
                    .emotionContent(originalContent)
                    .emotionScore(emotionScore)
                    .gptAnswer(gptAnswer)
                    .build();
        }
        EmotionRecord savedRecord = emotionRecordRepository2.save(recordToSave);
        return new EmotionRecordDetailDTO(savedRecord);
    }

    private float calculateWeightedScore(Map<String, String> probabilities) {
        if (probabilities == null || probabilities.isEmpty()) {
            return 55.0f;
        }
        Map<String, Double> weights = Map.of(
                "즐거움", 1.0,
                "사랑스러움", 0.5,
                "놀람", 0.0,
                "슬픔", -1.0,
                "분노", -0.7,
                "공포", -0.3
        );

        double weightedSum = 0.0;
        for (Map.Entry<String, String> entry : probabilities.entrySet()) {
            String emotion = entry.getKey();
            double probability = Double.parseDouble(entry.getValue().replace("%", "")) / 100.0;
            double weight = weights.getOrDefault(emotion, 0.0);

            weightedSum += probability * weight;
        }
        double scoreOutOf100 = (weightedSum + 1) * 45 + 10;
        return (float) (Math.round(scoreOutOf100 * 10.0) / 10.0);
    }
}
