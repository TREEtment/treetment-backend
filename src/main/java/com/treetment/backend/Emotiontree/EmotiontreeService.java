package com.treetment.backend.Emotiontree;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.treetment.backend.auth.entity.User;
import com.treetment.backend.auth.repository.UserRepository;
import com.treetment.backend.entity.EmotionTree;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotiontreeService {
    private final EmotiontreeRepository emotiontreeRepository;

    @Transactional(readOnly = true)
    public List<EmotiontreeDTO> getTreesByUserId(Integer userId)
    {
        List<EmotionTree> emotionTrees = emotiontreeRepository.findByUserIdWithUser(userId);
        return emotionTrees.stream()
                .map(EmotiontreeDTO::new) // emotionTree -> new EmotionTreeResponseDto(emotionTree)
                .collect(Collectors.toList());
    }
    @Transactional
    public void updateImage(Long treeId, String newImageUrl) {
        EmotionTree emotionTree = emotiontreeRepository.findById(treeId)
                .orElseThrow(() -> new EntityNotFoundException("감정 나무를 찾을 수 없습니다. ID: " + treeId));
        emotionTree.updateImage(newImageUrl);}

}
