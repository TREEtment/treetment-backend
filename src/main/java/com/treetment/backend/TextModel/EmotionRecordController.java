package com.treetment.backend.TextModel;

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

        EmotionRecordDetailDTO createdRecord = emotionRecordService.createRecord(userId, requestDTO);
        URI location = URI.create(String.format("/api/records/%d", createdRecord.getId()));

        return ResponseEntity.created(location).body(createdRecord);
    }
}
