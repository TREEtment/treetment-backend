package com.treetment.backend.TextModel;

import com.treetment.backend.entity.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmotionRecordRepository2 extends JpaRepository<EmotionRecord, Long>{
    Optional<EmotionRecord> findTopByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
