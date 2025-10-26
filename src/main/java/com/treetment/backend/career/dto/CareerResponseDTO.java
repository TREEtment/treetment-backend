package com.treetment.backend.career.DTO;

import com.treetment.backend.career.entity.Career;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CareerResponseDTO {
    private Long id;
    private Long counselorId;
    private String careerContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Career 엔티티를 CareerResponseDto로 변환하는 정적 메서드
    public static CareerResponseDTO from(Career career) {
        return CareerResponseDTO.builder()
                .id(career.getId())
                .counselorId(career.getCounselor().getId()) // 연관된 상담사의 ID 포함
                .careerContent(career.getCareerContent())
                .createdAt(career.getCreatedAt())
                .updatedAt(career.getUpdatedAt())
                .build();
    }
}
