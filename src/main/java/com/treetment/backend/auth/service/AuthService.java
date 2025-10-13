package com.treetment.backend.auth.service;

import com.treetment.backend.auth.domain.PROVIDER;
import com.treetment.backend.auth.domain.ROLE;
import com.treetment.backend.auth.dto.LoginRequest;
import com.treetment.backend.auth.dto.LoginResponse;
import com.treetment.backend.auth.dto.RegisterRequest;
import com.treetment.backend.auth.dto.UserResponse;
import com.treetment.backend.auth.entity.PasswordResetToken;
import com.treetment.backend.auth.entity.RefreshToken;
import com.treetment.backend.auth.entity.User;
import com.treetment.backend.auth.exception.AuthErrorCode;
import com.treetment.backend.auth.exception.AuthException;
import com.treetment.backend.auth.repository.PasswordResetTokenRepository;
import com.treetment.backend.auth.repository.RefreshTokenRepository;
import com.treetment.backend.auth.repository.UserRepository;
import com.treetment.backend.security.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.treetment.backend.security.util.JwtUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Value("${spring.jwt.access-expiration-ms}")
    private Long accessTokenExpirationMs;
    
    @Value("${spring.jwt.refresh-expiration-ms}")
    private Long refreshTokenExpirationMs;
    
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new AuthException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        
        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .name(request.getName())
                .role(ROLE.ROLE_USER)
                .provider(PROVIDER.LOCAL)
                .isActive(true)
                .build();
        
        // 비밀번호 암호화
        user.encodePassword(passwordEncoder);
        
        // 사용자 저장
        User savedUser = userRepository.save(user);
        
        log.info("User registered successfully: {}", savedUser.getEmail());
        return UserResponse.from(savedUser);
    }
    
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        // 사용자 정보 가져오기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed for email: {}", request.getEmail());
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        
        // 사용자 활성화 상태 확인
        if (!user.getIsActive()) {
            throw new AuthException(AuthErrorCode.USER_NOT_ACTIVE);
        }
        
        // 토큰 발급
        issueTokensOnLogin(response, null, user.getEmail(), user.getRole().name());
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        // LoginResponse 반환
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }
    
    @Transactional
    public void logout(HttpServletResponse response) {
        // 쿠키 삭제
        clearTokenCookies(response);
        log.info("User logged out successfully");
    }
    
    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractTokenFromCookies(request, REFRESH_TOKEN_COOKIE_NAME);
        
        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.TOKEN_NOT_FOUND);
        }
        
        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshToken) || !JWT_CATEGORY_REFRESH.equals(jwtUtil.getCategory(refreshToken))) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        
        // Refresh Token이 만료되었는지 확인
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }
        
        String email = jwtUtil.getEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        
        // 새로운 토큰 발급
        issueTokensOnLogin(response, refreshToken, email, user.getRole().name());
        
        log.info("Token refreshed successfully for user: {}", email);
    }
    
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        
        // 기존 토큰 삭제
        passwordResetTokenRepository.deleteByEmail(email);
        
        // 새로운 토큰 생성
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // 1시간 후 만료
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        // TODO: 이메일 발송 로직 구현
        log.info("Password reset token generated for user: {}", email);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndIsUsedFalseAndExpiresAtAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_EXPIRED));
        
        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        
        // 비밀번호 업데이트
        user.updatePassword(newPassword, passwordEncoder);
        userRepository.save(user);
        
        // 토큰 사용 처리
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
    
    private void issueTokensOnLogin(HttpServletResponse response, String existingRefreshToken, String email, String role) {
        // Access Token 생성
        String accessToken = jwtUtil.createJwt(JWT_CATEGORY_ACCESS, email, role, accessTokenExpirationMs);
        
        // Refresh Token 생성
        String newRefreshToken = jwtUtil.createJwt(JWT_CATEGORY_REFRESH, email, role, refreshTokenExpirationMs);
        
        // 기존 Refresh Token 삭제
        if (existingRefreshToken != null && !existingRefreshToken.isEmpty()) {
            refreshTokenRepository.deleteByRefreshToken(existingRefreshToken);
        }
        
        // 새로운 Refresh Token 저장
        RefreshToken refreshTokenEntity = new RefreshToken(email, newRefreshToken, 
                System.currentTimeMillis() + refreshTokenExpirationMs);
        refreshTokenRepository.save(refreshTokenEntity);
        
        // 쿠키 설정
        setTokenCookies(response, accessToken, newRefreshToken);
    }
    
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token 쿠키
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // 개발 환경에서는 false
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (accessTokenExpirationMs / 1000));
        response.addCookie(accessTokenCookie);
        
        // Refresh Token 쿠키
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // 개발 환경에서는 false
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        response.addCookie(refreshTokenCookie);
    }
    
    private void clearTokenCookies(HttpServletResponse response) {
        // Access Token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);
        
        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
    
    private String extractTokenFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
