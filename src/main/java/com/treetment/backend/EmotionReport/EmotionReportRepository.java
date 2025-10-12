package com.treetment.backend.EmotionReport;

import com.treetment.backend.entity.EmotionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface EmotionReportRepository extends JpaRepository<EmotionReport, Long>{
    List<EmotionReport> findByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<EmotionReport> findByIdAndUser_Id(Long reportId, Long userId);
    Optional<EmotionReport> findByUser_IdAndCreatedAtBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
