package com.treetment.backend.career.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerRequestDTO {
    private Long counselorId; // 어떤 상담사의 경력인지 명시
    private String careerContent;
}
