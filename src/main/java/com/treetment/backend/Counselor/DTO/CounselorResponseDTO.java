package com.treetment.backend.Counselor.DTO;

import com.treetment.backend.entity.Counselor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CounselorResponseDTO {
    private Long id;
    private String name;
    private String introduction;
    private String comment;
    private String contactAddress;
    private float score;
    private List<CareerDTO> careers; // 경력 정보는 CareerDto 리스트로 포함
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Counselor 엔티티를 CounselorResponseDto로 변환하는 정적 메서드
    public static CounselorResponseDTO from(Counselor counselor) {
        return CounselorResponseDTO.builder()
                .id(counselor.getId())
                .name(counselor.getName())
                .introduction(counselor.getIntroduction())
                .comment(counselor.getComment())
                .contactAddress(counselor.getContactAddress())
                .score(counselor.getScore())
                // Career 엔티티 리스트를 CareerDto 리스트로 변환
                .careers(counselor.getCareers().stream()
                        .map(CareerDTO::from)
                        .collect(Collectors.toList()))
                .createdAt(counselor.getCreatedAt())
                .updatedAt(counselor.getUpdatedAt())
                .build();
    }
}
