package com.treetment.backend.emotionRecord.service;
import com.treetment.backend.emotionRecord.dto.EmotionRecordCreateRequestDTO;
import com.treetment.backend.emotionRecord.dto.EmotionRecordDetailDTO;
import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import com.treetment.backend.emotionRecord.repository.EmotionRecordRepository2;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.treetment.backend.emotionTree.service.EmotiontreeService;
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
    private final EmotiontreeService emotiontreeService;

    @Transactional
    public EmotionRecordDetailDTO createRecord(Integer userId, EmotionRecordCreateRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        String originalContent = requestDTO.getEmotionContent();
        
        // AI로 점수 계산
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
        
        // 기존 동기 렌더 호출 제거 - 이제 비동기 방식으로 처리
        // TODO: GPU 워커가 별도로 처리하도록 변경됨
        // if (savedRecord.getEmotionScore() != null) {
        //     blenderService.requestTreeGrowth(savedRecord.getEmotionScore(), savedRecord.getUser().getId());
        // }
        
        return new EmotionRecordDetailDTO(savedRecord);
    }

    public record TreeInitResult(Long treeId, Double score) {}

    /**
     * 감정 기록 저장 + EmotionTree 대기 생성 후 treeId와 score 반환 (내부용)
     * 점수만 받아서 사용
     */
    @Transactional
    public TreeInitResult createRecordAndPendingTreeWithScore(Integer userId, Float emotionScore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        if (emotionScore == null) {
            throw new IllegalArgumentException("emotionScore는 필수입니다.");
        }
        
        // 기본값 설정 (점수만 받으므로 title, content는 기본값 사용)
        String emotionTitle = "오늘의 감정 기록";
        String emotionContent = "";
        String gptAnswer = "";

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        Optional<EmotionRecord> existingRecordOpt = emotionRecordRepository2
                .findTopByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay);
        EmotionRecord recordToSave;
        if (existingRecordOpt.isPresent()) {
            recordToSave = existingRecordOpt.get();
            recordToSave.update(emotionTitle, emotionContent, emotionScore, gptAnswer);
        }
        else {
            recordToSave = EmotionRecord.builder()
                    .user(user)
                    .emotionTitle(emotionTitle)
                    .emotionContent(emotionContent)
                    .emotionScore(emotionScore)
                    .gptAnswer(gptAnswer)
                    .build();
        }
        EmotionRecord savedRecord = emotionRecordRepository2.save(recordToSave);
        
        // EmotionTree 대기 생성
        com.treetment.backend.emotionTree.dto.TreeRenderResponseDTO treeRenderResponse =
                emotiontreeService.createPendingTree(savedRecord.getUser().getId(), savedRecord.getEmotionScore());

        return new TreeInitResult(treeRenderResponse.getTreeId(), Double.valueOf(savedRecord.getEmotionScore())) ;
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
            Object valueObj = entry.getValue();
            
            // 값이 String이 아닌 경우 (LinkedHashMap 등) 처리
            String valueStr;
            if (valueObj instanceof String) {
                valueStr = (String) valueObj;
            } else if (valueObj instanceof Number) {
                valueStr = String.valueOf(valueObj);
            } else {
                // LinkedHashMap이나 다른 타입인 경우 건너뛰기
                continue;
            }
            
            try {
                double probability = Double.parseDouble(valueStr.replace("%", "")) / 100.0;
                double weight = weights.getOrDefault(emotion, 0.0);
                weightedSum += probability * weight;
            } catch (NumberFormatException e) {
                // 숫자로 변환할 수 없는 경우 건너뛰기
                continue;
            }
        }
        double scoreOutOf100 = (weightedSum + 1) * 45 + 10;
        return (float) (Math.round(scoreOutOf100 * 10.0) / 10.0);
    }
}
