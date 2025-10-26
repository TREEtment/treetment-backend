package com.treetment.backend.emotionReport.dto;

import com.treetment.backend.emotionReport.entity.EmotionReport;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class EmotionReportDetailDTO {
    private final Long id;
    private final Integer userId;
    private final String reportTitle;
    private final String reportContent;
    private final Float emotionScore;
    private final LocalDate createdAt;
    public EmotionReportDetailDTO(EmotionReport report) {
        this.id = report.getId();
        this.userId = report.getUser().getId();
        this.reportTitle = report.getReportTitle();
        this.reportContent = report.getReportContent();
        this.emotionScore = report.getEmotionScore();
        this.createdAt = report.getCreatedAt();
    }
}
