package com.treetment.backend.user.controller;

import com.treetment.backend.global.dto.response.ApiResponse;
import com.treetment.backend.security.principle.CustomPrincipal;
import com.treetment.backend.user.dto.UpdateNicknameRequest;
import com.treetment.backend.user.dto.UserResponse;
import com.treetment.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 프로필 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "프로필 조회", description = "현재 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal CustomPrincipal principal) {
        Integer userId = principal.getUser().getId();
        UserResponse userResponse = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", userResponse));
    }

    @PutMapping("/nickname")
    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestBody UpdateNicknameRequest request) {
        Integer userId = principal.getUser().getId();
        UserResponse userResponse = userService.updateNickname(userId, request);
        return ResponseEntity.ok(ApiResponse.success("닉네임 수정 성공", userResponse));
    }

    @PostMapping("/profile/image")
    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileImage(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestParam("file") MultipartFile imageFile) {
        Integer userId = principal.getUser().getId();
        UserResponse userResponse = userService.updateProfileImage(userId, imageFile);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 업데이트 성공", userResponse));
    }

    @GetMapping("/nickname/check")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임의 사용 가능 여부를 확인합니다.", security = {})
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean available = userService.checkNicknameAvailability(nickname);
        return ResponseEntity.ok(ApiResponse.success(
            available ? "사용 가능한 닉네임입니다" : "이미 사용 중인 닉네임입니다", 
            available));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 완전히 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal CustomPrincipal principal) {
        Integer userId = principal.getUser().getId();
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다", null));
    }
}
