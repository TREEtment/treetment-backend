package com.treetment.backend.emotionTree.service;

import com.treetment.backend.emotionTree.dto.EmotiontreeDTO;
import com.treetment.backend.emotionTree.dto.TreeRenderResponseDTO;
import com.treetment.backend.emotionTree.dto.TreeStatusResponseDTO;
import com.treetment.backend.emotionTree.dto.CompleteTreeRequestDTO;
import com.treetment.backend.emotionTree.entity.EmotionTree;
import com.treetment.backend.emotionTree.repository.EmotiontreeRepository;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotiontreeService {
    private final EmotiontreeRepository emotiontreeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<EmotiontreeDTO> getTreesByUserId(Integer userId) {
        List<EmotionTree> emotionTrees = emotiontreeRepository.findByUserIdWithUser(userId);
        return emotionTrees.stream()
                .map(EmotiontreeDTO::new) // emotionTree -> new EmotionTreeResponseDto(emotionTree)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void updateImage(Long treeId, String newImageUrl) {
        EmotionTree emotionTree = emotiontreeRepository.findById(treeId)
                .orElseThrow(() -> new EntityNotFoundException("감정 나무를 찾을 수 없습니다. ID: " + treeId));
        emotionTree.updateImage(newImageUrl);
    }

    /**
     * 감정 기록에 대한 트리 렌더 작업을 대기 상태로 생성
     * @param userId 사용자 ID
     * @param emotionScore 감정 점수
     * @return 생성된 트리 ID와 렌더 상태
     */
    @Transactional
    public TreeRenderResponseDTO createPendingTree(Integer userId, Float emotionScore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        EmotionTree emotionTree = EmotionTree.builder()
                .user(user)
                .renderStatus("rendering")
                .imageUrl(null)
                .emotionTreeImage(null)
                .build();

        EmotionTree savedTree = emotiontreeRepository.save(emotionTree);
        
        return new TreeRenderResponseDTO(savedTree.getId(), "rendering");
    }

    /**
     * 트리 렌더 상태 조회
     * @param treeId 트리 ID
     * @return 렌더 상태와 이미지 URL
     */
    @Transactional(readOnly = true)
    public TreeStatusResponseDTO getTreeStatus(Long treeId) {
        EmotionTree emotionTree = emotiontreeRepository.findById(treeId)
                .orElseThrow(() -> new EntityNotFoundException("감정 나무를 찾을 수 없습니다. ID: " + treeId));

        return new TreeStatusResponseDTO(
                emotionTree.getId(),
                emotionTree.getRenderStatus(),
                emotionTree.getModelUrl(),
                emotionTree.getDataUrl(),
                emotionTree.getUpdatedAt() == null ? null : emotionTree.getUpdatedAt().toString()
        );
    }

    /**
     * 트리 렌더 완료 처리
     * @param request 완료 요청 DTO
     */
    @Transactional
    public void completeTreeRender(CompleteTreeRequestDTO request) {
        EmotionTree emotionTree = emotiontreeRepository.findById(request.getTreeId())
                .orElseThrow(() -> new EntityNotFoundException("감정 나무를 찾을 수 없습니다. ID: " + request.getTreeId()));

        // GLB 완료 처리
        emotionTree.markDoneWithModel(request.getModelUrl(), request.getDataUrl());
        emotiontreeRepository.save(emotionTree);
    }

    /**
     * 트리 렌더 실패 처리
     * @param treeId 트리 ID
     */
    @Transactional
    public void markTreeAsFailed(Long treeId) {
        EmotionTree emotionTree = emotiontreeRepository.findById(treeId)
                .orElseThrow(() -> new EntityNotFoundException("감정 나무를 찾을 수 없습니다. ID: " + treeId));

        emotionTree.markFailed();
        emotiontreeRepository.save(emotionTree);
    }
}