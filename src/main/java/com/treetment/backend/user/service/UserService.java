package com.treetment.backend.user.service;

import com.treetment.backend.emotionRecord.entity.EmotionRecord;
import com.treetment.backend.emotionRecord.repository.EmotionRecordRepository2;
import com.treetment.backend.emotionReport.entity.EmotionReport;
import com.treetment.backend.emotionReport.repository.EmotionReportRepository;
import com.treetment.backend.emotionTree.repository.EmotiontreeRepository;
import com.treetment.backend.user.dto.UpdateNicknameRequest;
import com.treetment.backend.user.dto.UserResponse;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final EmotiontreeRepository emotiontreeRepository;
    private final EmotionRecordRepository2 emotionRecordRepository2;
    private final EmotionReportRepository emotionReportRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateNickname(Integer userId, UpdateNicknameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 확인 (본인 닉네임이 아닌 경우에만)
        if (!user.getNickname().equals(request.getNickname()) && 
            userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(request.getNickname());
        User savedUser = userRepository.save(user);

        log.info("User nickname updated: {}", savedUser.getEmail());
        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse updateProfileImage(Integer userId, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미지 업로드
        String imageUrl = fileUploadService.uploadImage(imageFile, Long.valueOf(userId));
        
        // 프로필 이미지 URL 업데이트
        user.updateProfileImage(imageUrl);
        User savedUser = userRepository.save(user);

        log.info("User profile image updated: {}", savedUser.getEmail());
        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public boolean checkNicknameAvailability(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 연관된 데이터를 먼저 삭제 (외래 키 제약 조건 해결)
        // 1. EmotionTree 삭제 (직접 삭제 쿼리 사용)
        int deletedTrees = emotiontreeRepository.deleteByUser_Id(userId);
        log.info("Deleted {} emotion trees for user {}", deletedTrees, userId);

        // 2. EmotionRecord 삭제 (이미지 기록 포함)
        List<EmotionRecord> emotionRecords = emotionRecordRepository2.findByUser_Id(userId);
        if (!emotionRecords.isEmpty()) {
            emotionRecordRepository2.deleteAll(emotionRecords);
            log.info("Deleted {} emotion records for user {}", emotionRecords.size(), userId);
        }

        // 3. EmotionReport 삭제
        List<EmotionReport> emotionReports = emotionReportRepository.findByUser_IdOrderByCreatedAtDesc(Long.valueOf(userId));
        if (!emotionReports.isEmpty()) {
            emotionReportRepository.deleteAll(emotionReports);
            log.info("Deleted {} emotion reports for user {}", emotionReports.size(), userId);
        }

        // 4. User 삭제
        userRepository.delete(user);

        log.info("User deleted: {}", user.getEmail());
    }
}
