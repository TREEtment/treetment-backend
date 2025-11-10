package com.treetment.backend.emotionRecord.repository;

import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmotionRecordRepository2 extends JpaRepository<EmotionRecord, Long>{
    Optional<EmotionRecord> findTopByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(Integer userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<EmotionRecord> findByUser_Id(Integer userId);
}
