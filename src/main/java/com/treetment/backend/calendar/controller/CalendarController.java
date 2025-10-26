package com.treetment.backend.calendar.controller;

import com.treetment.backend.calendar.dto.CalendarDetailDTO;
import com.treetment.backend.calendar.dto.CalendarListDTO;
import com.treetment.backend.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping("/{userId}/{date}")
    public ResponseEntity<?> getRecordsByDate(
            @PathVariable Long userId,
            @PathVariable String date) {

        if (date.length() == 7) {
            List<CalendarListDTO> records = calendarService.findMonthlyRecordsByUserId(userId, date);
            return ResponseEntity.ok(records);
        } else if (date.length() == 10) {
            CalendarDetailDTO record = calendarService.findDailyRecordByUserId(userId, date);
            return ResponseEntity.ok(record);
        } else {
            return ResponseEntity.badRequest().body("잘못된 날짜 형식입니다. (YYYY-MM 또는 YYYY-MM-DD)");
        }
    }
}
