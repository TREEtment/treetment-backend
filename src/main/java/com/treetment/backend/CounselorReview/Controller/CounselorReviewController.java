package com.treetment.backend.CounselorReview.Controller;

import com.treetment.backend.CounselorReview.DTO.CounselorReviewRequestDTO;
import com.treetment.backend.CounselorReview.DTO.CounselorReviewResponseDTO;
import com.treetment.backend.CounselorReview.Service.CounselorReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CounselorReviewController {
    private final CounselorReviewService reviewService;

    // 리뷰 생성 API (POST /api/reviews)
    @PostMapping
    public ResponseEntity<CounselorReviewResponseDTO> createReview(@RequestBody CounselorReviewRequestDTO requestDTO) {
        CounselorReviewResponseDTO responseDto = reviewService.createReview(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 특정 상담사의 모든 리뷰 조회 API (GET /api/reviews/counselor/{counselorId})
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<CounselorReviewResponseDTO>> getReviewsByCounselorId(@PathVariable Long counselorId) {
        List<CounselorReviewResponseDTO> reviews = reviewService.getReviewsByCounselorId(counselorId);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 수정 API (PUT /api/reviews/{reviewId})
    @PutMapping("/{reviewId}")
    public ResponseEntity<CounselorReviewResponseDTO> updateReview(@PathVariable Long reviewId, @RequestBody CounselorReviewRequestDTO requestDTO) {
        CounselorReviewResponseDTO updatedReview = reviewService.updateReview(reviewId, requestDTO);
        return ResponseEntity.ok(updatedReview);
    }

    // 리뷰 삭제 API (DELETE /api/reviews/{reviewId})
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
