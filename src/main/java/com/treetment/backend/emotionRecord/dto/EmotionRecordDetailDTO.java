package com.treetment.backend.emotionRecord.dto;

import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class EmotionRecordDetailDTO {
    private final Long id;
    private final Integer userId;
    private final String emotionTitle;
    private final String emotionContent;
    private final Float emotionScore;
    private final String emotionImage;
    private final LocalDateTime createdAt;
    private final String gptAnswer;

    public EmotionRecordDetailDTO(EmotionRecord record) {
        this.id = record.getId();
        this.userId = record.getUser().getId();
        this.emotionTitle = record.getEmotionTitle();
        this.emotionContent = record.getEmotionContent();
        this.emotionScore = record.getEmotionScore();
        this.emotionImage = record.getEmotionImage();
        this.createdAt = record.getCreatedAt();
        this.gptAnswer = record.getGptAnswer();
    }
}
