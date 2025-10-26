package com.treetment.backend.Emotiontree;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
