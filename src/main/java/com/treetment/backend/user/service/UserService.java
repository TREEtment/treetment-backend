package com.treetment.backend.user.service;

import com.treetment.backend.user.dto.UpdateNicknameRequest;
import com.treetment.backend.user.dto.UserResponse;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    
    @PersistenceContext
    private EntityManager entityManager;

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
        // 1. EmotionTree 삭제 (Native Query 사용)
        int deletedTrees = entityManager.createNativeQuery("DELETE FROM emotion_tree WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        log.info("Deleted {} emotion trees for user {}", deletedTrees, userId);

        // 2. EmotionRecord 삭제 (이미지 기록 포함) - Native Query 사용
        int deletedRecords = entityManager.createNativeQuery("DELETE FROM emotion_record WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        log.info("Deleted {} emotion records for user {}", deletedRecords, userId);

        // 3. EmotionReport 삭제 - Native Query 사용
        int deletedReports = entityManager.createNativeQuery("DELETE FROM emotion_report WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        log.info("Deleted {} emotion reports for user {}", deletedReports, userId);
        
        // 4. UserNotification 삭제 (혹시 있을 수 있음)
        int deletedNotifications = entityManager.createNativeQuery("DELETE FROM user_notification WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        if (deletedNotifications > 0) {
            log.info("Deleted {} user notifications for user {}", deletedNotifications, userId);
        }
        
        // 5. Verification 삭제 (혹시 있을 수 있음)
        int deletedVerifications = entityManager.createNativeQuery("DELETE FROM verification WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        if (deletedVerifications > 0) {
            log.info("Deleted {} verifications for user {}", deletedVerifications, userId);
        }

        // 6. User 삭제
        userRepository.delete(user);

        log.info("User deleted: {}", user.getEmail());
    }
}
