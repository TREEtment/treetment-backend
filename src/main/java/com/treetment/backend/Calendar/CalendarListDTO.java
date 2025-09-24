package com.treetment.backend.Calendar;

import com.treetment.backend.entity.EmotionRecord;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CalendarListDTO {
    private final Long id;
    private final String emotionTitle;
    private final Float emotionScore;
    private final LocalDateTime createdAt;
    public CalendarListDTO(EmotionRecord record) {
        this.id = record.getId();
        this.emotionTitle = record.getEmotionTitle();
        this.emotionScore = record.getEmotionScore();
        this.createdAt = record.getCreatedAt();
    }
}
