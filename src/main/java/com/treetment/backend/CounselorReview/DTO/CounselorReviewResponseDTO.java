package com.treetment.backend.CounselorReview.DTO;

import com.treetment.backend.entity.CounselorReview;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CounselorReviewResponseDTO {
    private Long id;
    private Long counselorId;
    private byte score;
    private String reviewTitle;
    private String reviewContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // CounselorReview 엔티티를 DTO로 변환하는 정적 메서드
    public static CounselorReviewResponseDTO from(CounselorReview review) {
        return CounselorReviewResponseDTO.builder()
                .id(review.getId())
                .counselorId(review.getCounselor().getId())
                .score(review.getScore())
                .reviewTitle(review.getReviewTitle())
                .reviewContent(review.getReviewContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
