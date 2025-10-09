package com.treetment.backend.Counselor.DTO;

import com.treetment.backend.entity.Career;
import com.treetment.backend.entity.Counselor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

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

        // 문자열 리스트로 받은 경력들을 Career 엔티티 리스트로 변환
        if (careers != null && !careers.isEmpty()) {
            List<Career> careerEntities = careers.stream()
                    .map(careerContent -> Career.builder()
                            .careerContent(careerContent)
                            .counselor(counselor) // 연관관계 설정
                            .build())
                    .collect(Collectors.toList());
            counselor.setCareers(careerEntities);
        }

        return counselor;
    }
}
