package com.treetment.backend.user.service;

import com.treetment.backend.user.dto.UpdateProfileRequest;
import com.treetment.backend.user.dto.UserResponse;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.user.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Integer userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 확인 (본인 닉네임이 아닌 경우에만)
        if (!user.getNickname().equals(request.getNickname()) && 
            userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        user.updateProfile(request.getNickname(), request.getName());
        User savedUser = userRepository.save(user);

        log.info("User profile updated: {}", savedUser.getEmail());
        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse updateProfileImage(Integer userId, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미지 업로드
        String imageUrl = fileUploadService.uploadImage(imageFile);
        
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
}
