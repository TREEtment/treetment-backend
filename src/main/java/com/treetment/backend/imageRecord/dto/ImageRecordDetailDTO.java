package com.treetment.backend.imageRecord.dto;

import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * @param emotionImage 이미지 URL
 */
@Builder
public record ImageRecordDetailDTO(
        Long id,
        Integer userId,
        Float emotionScore,
        String emotionImage,
        LocalDateTime createdAt,
        String gptAnswer)
{
    public static ImageRecordDetailDTO from(EmotionRecord record) {
        return new ImageRecordDetailDTO(
                record.getId(),
                record.getUser().getId(),
                record.getEmotionScore(),
                record.getEmotionImage(),
                record.getCreatedAt(),
                record.getGptAnswer()
        );
    }
}
