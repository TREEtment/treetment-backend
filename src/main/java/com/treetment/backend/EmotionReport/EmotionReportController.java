package com.treetment.backend.EmotionReport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class EmotionReportController {
    private final EmotionReportService emotionReportService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmotionReportListDTO>> getAllReportsByUserId(@PathVariable Long userId) {
        List<EmotionReportListDTO> reports = emotionReportService.findAllReportsByUserId(userId);
        return ResponseEntity.ok(reports);
    }
    @GetMapping("/user/{userId}/{reportId}")
    public ResponseEntity<EmotionReportDetailDTO> getReportByIdAndUserId(
            @PathVariable Long userId,
            @PathVariable Long reportId) {
        EmotionReportDetailDTO report = emotionReportService.findReportByIdAndUserId(reportId, userId);
        return ResponseEntity.ok(report);}

    @PostMapping("/user/{userId}/generate")
    public ResponseEntity<EmotionReportDetailDTO> generateWeeklyReport(@PathVariable Long userId) {
        EmotionReportDetailDTO newReport = emotionReportService.generateWeeklyReportForUser(userId);
        URI location = URI.create(String.format("/api/reports/user/%d/%d", newReport.getUserId(), newReport.getId()));
        return ResponseEntity.created(location).body(newReport);
    }
}
