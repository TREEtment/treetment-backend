package com.treetment.backend.calendar.dto;

import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CalendarDetailDTO {
    private final Long id;
    private final Integer userId;
    private final String emotionTitle;
    private final String emotionContent;
    private final Float emotionScore;
    private final String emotionImage;
    private final String gptanswer;
    private final LocalDateTime createdAt;
    public CalendarDetailDTO(EmotionRecord record) {
        this.id = record.getId();
        this.userId = record.getUser().getId();
        this.emotionTitle = record.getEmotionTitle();
        this.emotionContent = record.getEmotionContent();
        this.emotionScore = record.getEmotionScore();
        this.emotionImage = record.getEmotionImage();
        this.createdAt = record.getCreatedAt();
        this.gptanswer= record.getGptAnswer();
    }
}
