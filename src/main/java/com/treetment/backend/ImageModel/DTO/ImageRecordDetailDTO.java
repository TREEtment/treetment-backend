package com.treetment.backend.ImageModel.DTO;

import com.treetment.backend.entity.EmotionRecord;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * @param emotionImage 이미지 URL
 */
@Builder
public record ImageRecordDetailDTO(
        Long id,
        Long userId,
        String emotionTitle,    // 대표 감정
        String emotionContent,  // 분석 결과를 JSON 형태로 저장
        Float emotionScore,
        String emotionImage,
        LocalDateTime createdAt,
        String gptAnswer)
{
    public static ImageRecordDetailDTO from(EmotionRecord record) {
        return new ImageRecordDetailDTO(
                record.getId(),
                record.getUser().getId(),
                record.getEmotionTitle(),
                record.getEmotionContent(),
                record.getEmotionScore(),
                record.getEmotionImage(),
                record.getCreatedAt(),
                record.getGptAnswer()
        );
    }
}
