package com.treetment.backend.ImageModel.DTO;

import com.treetment.backend.entity.EmotionRecord;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
public class ImageRecordDetailDTO {
    private final Long id;
    private final Long userId;
    private final String emotionTitle;
    private final String emotionContent;
    private final Float emotionScore;
    private final String emotionImage; // 이미지 URL
    private final LocalDateTime createdAt;
    private final String gptAnswer;

    public static ImageRecordDetailDTO from(EmotionRecord record) {
        return ImageRecordDetailDTO.builder()
                .id(record.getId())
                .userId(record.getUser().getId())
                .emotionTitle(record.getEmotionTitle())
                .emotionContent(record.getEmotionContent())
                .emotionScore(record.getEmotionScore())
                .emotionImage(record.getEmotionImage())
                .createdAt(record.getCreatedAt())
                .gptAnswer(record.getGptAnswer())
                .build();
    }
}
