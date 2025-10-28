package com.treetment.backend.emotionTree.controller;

import com.treetment.backend.emotionTree.dto.TreeGrowthRequestDTO;
import com.treetment.backend.emotionTree.service.BlenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trees")
@RequiredArgsConstructor
public class TreeGrowthController {
    private final BlenderService blenderService;

    /**
     * 기존 동기 트리 성장 API - 비동기 방식으로 변경됨
     * 
     * @deprecated 비동기 렌더 파이프라인으로 대체됨
     * 새로운 API: POST /api/records/text/user/{userId}/async-tree
     */
    @Deprecated
    @PostMapping("/grow")
    public ResponseEntity<String> growTree(@RequestBody TreeGrowthRequestDTO requestDTO) {
        // 기존 동기 호출 - 비동기 방식으로 변경됨
        // blenderService.requestTreeGrowth(requestDTO.getScore(), requestDTO.getUserId());
        
        return ResponseEntity.ok("Tree growth request has been deprecated. Please use async API: POST /api/records/text/user/{userId}/async-tree");
    }
}
