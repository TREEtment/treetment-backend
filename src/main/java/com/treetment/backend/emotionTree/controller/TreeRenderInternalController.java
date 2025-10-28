package com.treetment.backend.emotionTree.controller;

import com.treetment.backend.emotionTree.dto.CompleteTreeRequestDTO;
import com.treetment.backend.emotionTree.service.EmotiontreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 트리 렌더링 완료 보고를 위한 내부 API 컨트롤러
 * GPU 워커가 렌더링 완료 후 호출하는 엔드포인트
 */
@RestController
@RequestMapping("/api/trees/internal")
@RequiredArgsConstructor
public class TreeRenderInternalController {
    private final EmotiontreeService emotiontreeService;

    /**
     * 트리 렌더 완료 보고 API
     * GPU 워커가 렌더링을 완료하고 S3에 이미지를 업로드한 후 호출
     * 
     * TODO: 운영 환경에서는 다음 보안 조치 필요:
     * - VPC 내부 전용으로만 접근 허용
     * - 또는 shared secret header 검증 추가
     * - 또는 API 키 기반 인증 추가
     */
    @PostMapping("/complete")
    public ResponseEntity<Void> completeTreeRender(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody CompleteTreeRequestDTO request) {
        String expected = System.getenv("INTERNAL_SECRET");
        if (expected == null || token == null || !expected.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        emotiontreeService.completeTreeRender(request);
        return ResponseEntity.ok().build();
    }
}

