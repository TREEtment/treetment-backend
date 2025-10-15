package com.treetment.backend.Emotiontree;

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

    @PostMapping("/grow")
    public ResponseEntity<String> growTree(@RequestBody TreeGrowthRequestDTO requestDTO) {
        blenderService.requestTreeGrowth(requestDTO.getScore(), requestDTO.getUserId());
        return ResponseEntity.ok("Tree growth request has been processed successfully.");
    }
}
