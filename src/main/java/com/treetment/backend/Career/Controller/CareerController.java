package com.treetment.backend.Career.Controller;

import com.treetment.backend.Career.DTO.CareerRequestDTO;
import com.treetment.backend.Career.DTO.CareerResponseDTO;
import com.treetment.backend.Career.Service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {
    private final CareerService careerService;

    // 경력 생성 API (POST /api/careers)
    @PostMapping
    public ResponseEntity<CareerResponseDTO> createCareer(@RequestBody CareerRequestDTO requestDto) {
        CareerResponseDTO responseDto = careerService.createCareer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 특정 상담사의 모든 경력 조회 API (GET /api/careers/counselor/{counselorId})
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<CareerResponseDTO>> getCareersByCounselorId(@PathVariable Long counselorId) {
        List<CareerResponseDTO> careers = careerService.getCareersByCounselorId(counselorId);
        return ResponseEntity.ok(careers);
    }

    // 경력 내용 수정 API (PUT /api/careers/{careerId})
    @PutMapping("/{careerId}")
    public ResponseEntity<CareerResponseDTO> updateCareer(@PathVariable Long careerId, @RequestBody Map<String, String> requestBody) {
        // careerContent 키의 값을 가져옴
        String newContent = requestBody.get("careerContent");
        CareerResponseDTO updatedCareer = careerService.updateCareer(careerId, newContent);
        return ResponseEntity.ok(updatedCareer);
    }

    // 경력 삭제 API (DELETE /api/careers/{careerId})
    @DeleteMapping("/{careerId}")
    public ResponseEntity<Void> deleteCareer(@PathVariable Long careerId) {
        careerService.deleteCareer(careerId);
        return ResponseEntity.noContent().build();
    }
}
