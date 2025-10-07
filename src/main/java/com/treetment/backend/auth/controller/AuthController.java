package com.treetment.backend.auth.controller;

import com.treetment.backend.auth.dto.LoginRequest;
import com.treetment.backend.auth.dto.RegisterRequest;
import com.treetment.backend.auth.dto.UserResponse;
import com.treetment.backend.auth.entity.User;
import com.treetment.backend.auth.service.AuthService;
import com.treetment.backend.global.dto.response.ApiResponse;
import com.treetment.backend.security.principle.CustomPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", user));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request, 
                                                   HttpServletResponse response) {
        authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", null));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request, 
                                                           HttpServletResponse response) {
        authService.refreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다.", null));
    }
    
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 이메일이 발송되었습니다.", null));
    }
    
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String token, 
                                                           @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다.", null));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        UserResponse userResponse = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보를 조회했습니다.", userResponse));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Auth service is healthy", "OK"));
    }
}
