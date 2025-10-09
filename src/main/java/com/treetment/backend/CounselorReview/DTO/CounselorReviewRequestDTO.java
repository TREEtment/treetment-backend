package com.treetment.backend.CounselorReview.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounselorReviewRequestDTO {
    private Long counselorId;
    private byte score;
    private String reviewTitle;
    private String reviewContent;
}
