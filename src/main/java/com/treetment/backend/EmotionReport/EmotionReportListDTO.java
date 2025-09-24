package com.treetment.backend.EmotionReport;

import com.treetment.backend.entity.EmotionReport;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class EmotionReportListDTO {
    private final Long id;
    private final String reportTitle;
    private final Float emotionScore;
    private final LocalDate createdAt;
    public EmotionReportListDTO(EmotionReport report) {
        this.id = report.getId();
        this.reportTitle = report.getReportTitle();
        this.emotionScore = report.getEmotionScore();
        this.createdAt = report.getCreatedAt();
    }
}

