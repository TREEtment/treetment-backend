package com.treetment.backend.calendar.repository;

import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarRepository extends JpaRepository<EmotionRecord, Long> {
    List<EmotionRecord> findByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
