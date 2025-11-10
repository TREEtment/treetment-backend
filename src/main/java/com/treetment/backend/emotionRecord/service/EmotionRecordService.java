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
        System.out.println("=== createRecord 시작 ===");
        System.out.println("originalContent: " + originalContent);
        System.out.println("emotionTitle: " + requestDTO.getEmotionTitle());
        
        // AI로 점수 계산
        String translatedContent = papagoService.translateToEnglish(originalContent);
        System.out.println("번역된 내용: " + translatedContent);
        
        Map<String, String> emotionProbabilities = customAiService.predictEmotion(translatedContent);
        System.out.println("AI 서버 응답: " + emotionProbabilities);
        
        float emotionScore = calculateWeightedScore(emotionProbabilities);
        System.out.println("계산된 점수: " + emotionScore);
        
        String gptAnswer = gptService.getGptAdvice(requestDTO.getEmotionContent());
        System.out.println("GPT 답변: " + gptAnswer);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        Optional<EmotionRecord> existingRecordOpt = emotionRecordRepository2
                .findTopByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay);
        EmotionRecord recordToSave;
        if (existingRecordOpt.isPresent()) {
            recordToSave = existingRecordOpt.get();
            System.out.println("기존 기록 업데이트");
            recordToSave.update(requestDTO.getEmotionTitle(), originalContent, emotionScore, gptAnswer);
            System.out.println("업데이트 후 - emotionContent: " + recordToSave.getEmotionContent());
            System.out.println("업데이트 후 - gptAnswer: " + recordToSave.getGptAnswer());
        }
        else {
            System.out.println("새 기록 생성");
            recordToSave = EmotionRecord.builder()
                    .user(user)
                    .emotionTitle(requestDTO.getEmotionTitle())
                    .emotionContent(originalContent)
                    .emotionScore(emotionScore)
                    .gptAnswer(gptAnswer)
                    .build();
            System.out.println("생성 후 - emotionContent: " + recordToSave.getEmotionContent());
            System.out.println("생성 후 - gptAnswer: " + recordToSave.getGptAnswer());
        }
        
        EmotionRecord savedRecord = emotionRecordRepository2.save(recordToSave);
        System.out.println("저장 후 - emotionContent: " + savedRecord.getEmotionContent());
        System.out.println("저장 후 - gptAnswer: " + savedRecord.getGptAnswer());
        System.out.println("저장 후 - emotionScore: " + savedRecord.getEmotionScore());
        
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
        int processedCount = 0;
        
        for (Map.Entry<String, String> entry : probabilities.entrySet()) {
            String emotion = entry.getKey();
            String valueStr = entry.getValue();
            
            if (valueStr == null || valueStr.isBlank()) {
                continue;
            }
            
            try {
                // "%" 제거하고 숫자로 변환
                String cleanedValue = valueStr.replace("%", "").trim();
                double probability = Double.parseDouble(cleanedValue) / 100.0;
                double weight = weights.getOrDefault(emotion, 0.0);
                weightedSum += probability * weight;
                processedCount++;
            } catch (NumberFormatException e) {
                // 숫자로 변환할 수 없는 경우 건너뛰기
                continue;
            }
        }
        
        // 처리된 항목이 없으면 기본값 반환
        if (processedCount == 0) {
            return 55.0f;
        }
        
        double scoreOutOf100 = (weightedSum + 1) * 45 + 10;
        return (float) (Math.round(scoreOutOf100 * 10.0) / 10.0);
    }
}
