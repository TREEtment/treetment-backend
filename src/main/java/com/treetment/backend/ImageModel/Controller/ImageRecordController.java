package com.treetment.backend.ImageModel.Controller;

import com.treetment.backend.ImageModel.DTO.ImageRecordCreateRequestDTO;
import com.treetment.backend.ImageModel.DTO.ImageRecordDetailDTO;
import com.treetment.backend.ImageModel.Service.ImageRecordService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/records/image")
@RequiredArgsConstructor
public class ImageRecordController {
    private final ImageRecordService imageRecordService;

    /**
     * 이미지 기반 감정 기록 생성 API
     * 하루에 한 번만 생성이 가능하며,
     * 중복 생성 시 409 Conflict 응답을 반환하고,
     * 성공 시 201 Created 응답과 함께 생성된 기록의 상세 정보를 반환
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<ImageRecordDetailDTO> createDailyImageRecord(
            @PathVariable Long userId,
            @RequestBody ImageRecordCreateRequestDTO requestDTO) {

        ImageRecordDetailDTO createdRecord = imageRecordService.createImageRecord(userId, requestDTO);

        // 생성된 리소스의 위치(URI)를 Location 헤더에 담아 응답 생성
        URI location = URI.create(String.format("/api/records/image/%d", createdRecord.id()));

        // 성공적으로 생성되었으므로 201 Created 상태 코드로 응답
        return ResponseEntity.created(location).body(createdRecord);
    }

    /**
     * 서비스에서 이미 기록이 존재할 때,
     * 이 메서드가 실행되어 409 Conflict 응답
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRecord(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict - 요청이 서버의 현재 상태와 충돌
                .body(Map.of("error", "기록 중복", "message", ex.getMessage()));
    }

    /**
     * 사용자를 찾을 수 없음
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404 Not Found
                .body(Map.of("error", "찾을 수 없음", "message", ex.getMessage()));
    }
}
