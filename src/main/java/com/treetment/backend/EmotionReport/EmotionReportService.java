package com.treetment.backend.EmotionReport;

import com.treetment.backend.EmotionReport.EmotionReportListDTO;
import com.treetment.backend.entity.EmotionReport;
import com.treetment.backend.EmotionReport.EmotionReportRepository;
import com.treetment.backend.EmotionReport.EmotionReportDetailDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class EmotionReportService {
    private final EmotionReportRepository emotionReportRepository;

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
}
