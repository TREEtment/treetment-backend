package com.treetment.backend.ImageModel.Repository;

import com.treetment.backend.entity.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ImageRecordRepository extends JpaRepository<EmotionRecord, Long>{
    // 특정 유저의 오늘 날짜에 해당하는 감정 기록 가장 최근 것 조회
    Optional<EmotionRecord> findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay);
}
