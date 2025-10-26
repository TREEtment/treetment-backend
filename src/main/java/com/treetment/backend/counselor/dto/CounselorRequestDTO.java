package com.treetment.backend.counselor.DTO;

import com.treetment.backend.career.entity.Career;
import com.treetment.backend.counselor.entity.Counselor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CounselorRequestDTO {
    private String name;
    private String introduction;
    private String comment;
    private String contactAddress;
    private List<String> careers; // 경력은 문자열 리스트로 받습니다.

    // DTO를 Counselor 엔티티로 변환하는 메서드
    public Counselor toEntity() {
        Counselor counselor = Counselor.builder()
                .name(name)
                .introduction(introduction)
                .comment(comment)
                .contactAddress(contactAddress)
                .build();

        // 경력 정보가 있는 경우, Career 엔티티로 변환하여 추가
        if (careers != null && !careers.isEmpty()) {
            List<Career> careerEntities = careers.stream()
                    .map(careerContent -> Career.builder()
                            .careerContent(careerContent)
                            .counselor(counselor) // 연관관계 설정
                            .build())
                    .toList(); // collect(Collectors.toList())
            counselor.setCareers(careerEntities);
        }
        return counselor;
    }
}
