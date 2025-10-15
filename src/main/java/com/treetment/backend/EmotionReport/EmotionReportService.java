package com.treetment.backend.EmotionReport;

import com.treetment.backend.entity.EmotionReport;
import com.treetment.backend.entity.EmotionRecord;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor

public class EmotionReportService {
    private final EmotionReportRepository emotionReportRepository;
    private final EmotionRecordRepository emotionRecordRepository;
    private final GptServiceReport gptService;

    @Transactional(readOnly = true)
    public List<EmotionReportListDTO> findAllReportsByUserId(Long userId) {
        List<EmotionReport> reports = emotionReportRepository.findByUser_IdOrderByCreatedAtDesc(userId);

        return reports.stream()
                .map(EmotionReportListDTO::new)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public EmotionReportDetailDTO findReportByIdAndUserId(Long reportId, Long userId) {
        EmotionReport report = emotionReportRepository.findByIdAndUser_Id(reportId, userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자의 리포트를 찾을 수 없습니다. Report ID: " + reportId + ", User ID: " + userId));

        return new EmotionReportDetailDTO(report);
    }
    @Transactional
    public EmotionReportDetailDTO generateWeeklyReportForUser(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfLastWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);

        LocalDateTime startDateTime = startOfLastWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfLastWeek.plusDays(1).atStartOfDay();
        List<EmotionRecord> weeklyRecords = emotionRecordRepository.findByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDateTime, endDateTime);

        if (weeklyRecords.isEmpty()) {
            throw new EntityNotFoundException("리포트를 생성할 지난주 감정 기록이 없습니다.");
        }

        double averageScore = weeklyRecords.stream().mapToDouble(EmotionRecord::getEmotionScore).average().orElse(0.0);
        float finalScore = (float) (Math.round(averageScore * 10.0) / 10.0);
        int weekOfMonth = (endOfLastWeek.getDayOfMonth() - 1) / 7 + 1;
        String title = String.format("%d월 %d주차 리포트", endOfLastWeek.getMonthValue(), weekOfMonth);

        List<String> gptAnswers = weeklyRecords.stream().map(EmotionRecord::getGptAnswer).collect(Collectors.toList());
        String gptContent = gptService.summarizeGptAnswers(gptAnswers);

        EmotionReport report = emotionReportRepository.findByUser_IdAndCreatedAtBetween(userId, startOfLastWeek, endOfLastWeek)
                .map(existingReport -> {
                    existingReport.updateContent(title, gptContent, finalScore);
                    return existingReport;
                })
                .orElseGet(() -> {
                    return EmotionReport.builder()
                            .user(weeklyRecords.get(0).getUser())
                            .emotionScore(finalScore)
                            .reportTitle(title)
                            .reportContent(gptContent)
                            .createdAt(startOfLastWeek)
                            .build();
                });

        EmotionReport savedReport = emotionReportRepository.save(report);
        return new EmotionReportDetailDTO(savedReport);
    }
}
