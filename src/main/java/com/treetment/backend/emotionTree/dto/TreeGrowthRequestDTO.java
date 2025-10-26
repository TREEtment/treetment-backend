package com.treetment.backend.emotionTree.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter @NoArgsConstructor
public class TreeGrowthRequestDTO {
    private Integer userId;
    private float score;
}
