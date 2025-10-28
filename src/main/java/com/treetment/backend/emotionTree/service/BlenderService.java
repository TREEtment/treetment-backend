package com.treetment.backend.emotionTree.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.treetment.backend.emotionTree.dto.GrowthRequestDto;
import com.treetment.backend.emotionTree.dto.GrowthResponseDto;
import com.treetment.backend.emotionTree.repository.EmotiontreeRepository;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository;
import com.treetment.backend.emotionTree.entity.EmotionTree;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.treetment.backend.emotionTree.service.BlenderService;
@Service
@RequiredArgsConstructor

public class BlenderService {
    private final RestTemplate restTemplate;
    private final EmotiontreeRepository emotiontreeRepository; // DB 조회를 위해 추가
    private final UserRepository userRepository; // User 조회를 위해 추가
    private final AmazonS3 amazonS3; // S3 사용을 위해 추가

    @Value("${blender.server.url}")
    private String blenderServerUrl;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final String DEFAULT_JSON_PATH = "defaults/emotion_tree_data.json";

    /**
     * 기존 동기 트리 성장 요청 메서드 - 비동기 방식으로 변경됨
     * 이제 GPU 워커가 별도로 처리하므로 사용하지 않음
     * 
     * @deprecated 비동기 렌더 파이프라인으로 대체됨
     */
    @Deprecated
    @Transactional
    public void requestTreeGrowth(float score, Integer userId) {
        // 기존 동기 호출 코드 - 비동기 방식으로 변경됨
        // TODO: GPU 워커가 별도로 처리하도록 변경됨
        
        /*
        boolean treeExists = emotiontreeRepository.findByUser_Id(userId).isPresent();

        if (!treeExists) {
            createInitialTreeResources(userId);
        }

        GrowthRequestDto requestDto = new GrowthRequestDto(score, String.valueOf(userId));
        String url = blenderServerUrl + "/grow-tree";
        try {
            restTemplate.postForObject(url, requestDto, GrowthResponseDto.class);
            System.out.println("FastAPI tree growth request sent for userId=" + userId);
        } catch (RestClientException e) {
            System.err.println("FastAPI request failed: " + e.getMessage());
        }
        */
        
        System.out.println("requestTreeGrowth 메서드가 호출되었지만 비동기 방식으로 변경되어 실제 처리는 GPU 워커가 담당합니다.");
    }


    private void createInitialTreeResources(Integer userId) {
        System.out.println("신규 사용자 감지. 초기 나무 리소스를 생성합니다. userId=" + userId);


        String userJsonPath = "trees/" + userId + "/emotion_tree_data.json";
        copyS3Object(DEFAULT_JSON_PATH, userJsonPath);


        String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/trees/%d/emotion_tree.png",
                bucket, amazonS3.getRegionName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));


        EmotionTree newTree = EmotionTree.builder()
                .user(user)
                .emotionTreeImage(imageUrl)
                .build();
        emotiontreeRepository.save(newTree);
    }

    private void copyS3Object(String sourceKey, String destinationKey) {
        try {
            amazonS3.copyObject(new CopyObjectRequest(bucket, sourceKey, bucket, destinationKey));
        } catch (Exception e) {
            throw new RuntimeException("S3 template copy failed.", e);
        }
    }
}
