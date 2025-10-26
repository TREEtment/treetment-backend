package com.treetment.backend.counselor.DTO;

import com.treetment.backend.career.entity.Career;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CareerDTO {
    private Long id;
    private String careerContent;

    public static CareerDTO from(Career career) {
        return CareerDTO.builder()
                .id(career.getId())
                .careerContent(career.getCareerContent())
                .build();
    }
}
