package com.treetment.backend.emotionTree.controller;

import com.treetment.backend.emotionTree.dto.EmotionTreeImageUpdateDTO;
import com.treetment.backend.emotionTree.dto.EmotiontreeDTO;
import com.treetment.backend.emotionTree.dto.TreeStatusResponseDTO;
import com.treetment.backend.emotionTree.service.EmotiontreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/trees")
@RequiredArgsConstructor
public class EmotiontreeController {
    private final EmotiontreeService emotiontreeService;
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmotiontreeDTO>> getTreesByUserId(@PathVariable Integer userId) {
        List<EmotiontreeDTO> trees = emotiontreeService.getTreesByUserId(userId);
        return ResponseEntity.ok(trees);
    }
    @PatchMapping("/{treeId}")
    public ResponseEntity<Void> updateEmotionTreeImage(
            @PathVariable Long treeId,
            @RequestBody EmotionTreeImageUpdateDTO updateDTO) {

        emotiontreeService.updateImage(treeId, updateDTO.getEmotionTreeImage());
        return ResponseEntity.noContent().build();
    }

    /**
     * 트리 렌더 상태 조회 API
     * 프론트엔드에서 폴링하여 렌더 진행 상황을 확인할 수 있음
     */
    @GetMapping("/status/{treeId}")
    public ResponseEntity<TreeStatusResponseDTO> getTreeStatus(@PathVariable Long treeId) {
        try {
            TreeStatusResponseDTO status = emotiontreeService.getTreeStatus(treeId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "트리를 찾을 수 없습니다. ID: " + treeId);
        }
    }
}
