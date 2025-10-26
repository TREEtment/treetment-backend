package com.treetment.backend.emotionRecord.controller;

import com.treetment.backend.emotionRecord.dto.EmotionRecordCreateRequestDTO;
import com.treetment.backend.emotionRecord.dto.EmotionRecordDetailDTO;
import com.treetment.backend.emotionRecord.service.EmotionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class EmotionRecordController {
    private final EmotionRecordService emotionRecordService;

    @PostMapping("/text/user/{userId}")
    public ResponseEntity<EmotionRecordDetailDTO> createEmotionRecord(
            @PathVariable Long userId,
            @RequestBody EmotionRecordCreateRequestDTO requestDTO) {

        EmotionRecordDetailDTO createdRecord = emotionRecordService.createRecord(userId.intValue(), requestDTO);
        URI location = URI.create(String.format("/api/records/%d", createdRecord.getId()));

        return ResponseEntity.created(location).body(createdRecord);
    }
}
