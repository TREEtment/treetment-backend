package com.treetment.backend.calendar.service;

import com.treetment.backend.calendar.dto.CalendarDetailDTO;
import com.treetment.backend.calendar.dto.CalendarListDTO;
import com.treetment.backend.calendar.repository.CalendarRepository;
import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    @Transactional(readOnly = true)
    public List<CalendarListDTO> findMonthlyRecordsByUserId(Long userId, String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<EmotionRecord> records = calendarRepository
                .findByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDateTime, endDateTime);

        return records.stream()
                .map(CalendarListDTO::new)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public CalendarDetailDTO findDailyRecordByUserId(Long userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(23, 59, 59);

        List<EmotionRecord> records = calendarRepository
                .findByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDateTime, endDateTime);

        if (records.isEmpty()) {
            throw new EntityNotFoundException("해당 날짜에 작성된 감정 기록이 없습니다: " + date);
        }

        return new CalendarDetailDTO(records.get(0));
    }

    @Transactional(readOnly = true)
    public List<CalendarDetailDTO> findDailyRecordsByUserId(Long userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(23, 59, 59);

        List<EmotionRecord> records = calendarRepository
                .findByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDateTime, endDateTime);

        return records.stream()
                .map(CalendarDetailDTO::new)
                .collect(Collectors.toList());
    }
}
