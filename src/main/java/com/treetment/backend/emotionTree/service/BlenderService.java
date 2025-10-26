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

    @Transactional
    public void requestTreeGrowth(float score, Integer userId) {

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
