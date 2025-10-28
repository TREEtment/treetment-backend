package com.treetment.backend.emotionTree.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTreeRequestDTO {
    private Long treeId;
    private String imageUrl;
}

