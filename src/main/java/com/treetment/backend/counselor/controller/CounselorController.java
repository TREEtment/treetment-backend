package com.treetment.backend.counselor.Controller;

import com.treetment.backend.counselor.DTO.CounselorRequestDTO;
import com.treetment.backend.counselor.DTO.CounselorResponseDTO;
import com.treetment.backend.counselor.Service.CounselorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 상담사 관련 REST API 컨트롤러
@RestController
@RequestMapping("/api/counselors") // 공통 URL 경로 설정
@RequiredArgsConstructor
public class CounselorController {
    private final CounselorService counselorService;

    // 상담사 생성 API (POST /api/counselors)
    @PostMapping
    public ResponseEntity<CounselorResponseDTO> createCounselor(@RequestBody CounselorRequestDTO requestDTO) {
        CounselorResponseDTO responseDto = counselorService.createCounselor(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 전체 상담사 조회 API (GET /api/counselors)
    @GetMapping
    public ResponseEntity<List<CounselorResponseDTO>> getAllCounselors() {
        List<CounselorResponseDTO> counselors = counselorService.getAllCounselors();
        return ResponseEntity.ok(counselors);
    }

    // 특정 상담사 조회 API (GET /api/counselors/{id})
    @GetMapping("/{id}")
    public ResponseEntity<CounselorResponseDTO> getCounselorById(@PathVariable Long id) {
        CounselorResponseDTO counselor = counselorService.getCounselorById(id);
        return ResponseEntity.ok(counselor);
    }

    // 상담사 정보 수정 API (PUT /api/counselors/{id})
    @PutMapping("/{id}")
    public ResponseEntity<CounselorResponseDTO> updateCounselor(
            @PathVariable Long id, @RequestBody CounselorRequestDTO requestDTO) {
        CounselorResponseDTO updatedCounselor = counselorService.updateCounselor(id, requestDTO);
        return ResponseEntity.ok(updatedCounselor);
    }

    // 상담사 삭제 API (DELETE /api/counselors/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCounselor(@PathVariable Long id) {
        counselorService.deleteCounselor(id);
        return ResponseEntity.noContent().build();
    }
}
