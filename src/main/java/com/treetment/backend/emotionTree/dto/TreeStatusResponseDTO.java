package com.treetment.backend.emotionTree.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TreeStatusResponseDTO {
    private Long treeId;
    private String status;
    private String imageUrl;
}

