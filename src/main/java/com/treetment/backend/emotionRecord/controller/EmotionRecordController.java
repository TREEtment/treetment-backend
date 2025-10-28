package com.treetment.backend.emotionRecord.controller;

import com.treetment.backend.emotionRecord.dto.EmotionRecordCreateRequestDTO;
import com.treetment.backend.emotionRecord.dto.EmotionRecordDetailDTO;
import com.treetment.backend.emotionRecord.service.EmotionRecordService;
import com.treetment.backend.emotionTree.dto.TreeRenderResponseDTO;
import com.treetment.backend.emotionTree.service.EmotiontreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class EmotionRecordController {
    private final EmotionRecordService emotionRecordService;
    private final EmotiontreeService emotiontreeService;

    @PostMapping("/text/user/{userId}")
    public ResponseEntity<EmotionRecordDetailDTO> createEmotionRecord(
            @PathVariable Long userId,
            @RequestBody EmotionRecordCreateRequestDTO requestDTO) {

        EmotionRecordDetailDTO createdRecord = emotionRecordService.createRecord(userId.intValue(), requestDTO);
        URI location = URI.create(String.format("/api/records/%d", createdRecord.getId()));

        return ResponseEntity.created(location).body(createdRecord);
    }

    /**
     * 비동기 트리 렌더링을 위한 감정 기록 생성 API
     * 기존 API와 동일한 입력을 받지만, 트리 렌더링은 비동기로 처리됨
     */
    @PostMapping("/text/user/{userId}/async-tree")
    public ResponseEntity<TreeRenderResponseDTO> createEmotionRecordWithAsyncTree(
            @PathVariable Long userId,
            @RequestBody EmotionRecordCreateRequestDTO requestDTO) {

        // 1) 감정 기록 저장 + EmotionTree 대기 생성
        EmotionRecordService.TreeInitResult init = emotionRecordService.createRecordAndPendingTree(userId.intValue(), requestDTO);

        // 2) 즉시 응답
        TreeRenderResponseDTO resp = new TreeRenderResponseDTO(init.treeId(), "rendering");

        // 3) 비동기 worker.sh 실행
        Long finalTreeId = init.treeId();
        Long finalUserId = userId;
        Double finalScore = init.score();
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "/app/worker.sh",
                        finalTreeId.toString(),
                        finalUserId.toString(),
                        finalScore.toString()
                );

                // 환경 변수 전달 (docker-compose에서 주입)
                Map<String, String> env = pb.environment();
                env.put("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"));
                env.put("AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY"));
                env.put("AWS_DEFAULT_REGION", System.getenv("AWS_DEFAULT_REGION"));
                env.put("INTERNAL_SECRET", System.getenv("INTERNAL_SECRET"));

                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    // worker.sh 실행 실패 시 상태를 failed로 변경
                    markTreeAsFailed(finalTreeId);
                }
            } catch (Exception e) {
                // 예외 발생 시 상태를 failed로 변경
                markTreeAsFailed(finalTreeId);
                e.printStackTrace();
            }
        }).start();

        return ResponseEntity.ok(resp);
    }

    /**
     * 트리 상태를 failed로 변경하는 헬퍼 메서드
     */
    private void markTreeAsFailed(Long treeId) {
        try {
            emotiontreeService.markTreeAsFailed(treeId);
            System.err.println("트리 렌더링 실패 처리 완료: treeId=" + treeId);
        } catch (Exception e) {
            System.err.println("트리 상태 변경 실패: treeId=" + treeId + ", error=" + e.getMessage());
        }
    }
}
