package com.treetment.backend.auth.controller;

import com.treetment.backend.auth.verification.EmailVerificationService;
import com.treetment.backend.global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/verification")
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "이메일 인증 코드 발송 및 확인 API")
public class VerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    @Operation(summary = "인증 코드 발송", description = "이메일로 인증 코드를 발송합니다.")
    public ResponseEntity<ApiResponse<Void>> send(@RequestBody SendVerificationRequest request) {
        emailVerificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("인증 코드 발송 완료", null));
    }

    @PostMapping("/confirm")
    @Operation(summary = "인증 코드 확인", description = "발송된 인증 코드를 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> confirm(@RequestBody ConfirmVerificationRequest request) {
        boolean ok = emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(ok ? "인증 완료" : "인증 실패", ok));
    }

    // DTO 클래스들
    public static class SendVerificationRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ConfirmVerificationRequest {
        private String email;
        private String code;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}


