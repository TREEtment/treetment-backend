package com.treetment.backend.security.handler;

import com.treetment.backend.auth.domain.ROLE;
import com.treetment.backend.auth.service.AuthService;
import com.treetment.backend.global.util.ResponseUtil;
import com.treetment.backend.security.logger.SecurityEventLogger;
import com.treetment.backend.security.principle.CustomPrincipal;
import com.treetment.backend.security.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.treetment.backend.security.util.JwtUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    private final ResponseUtil responseUtil;
    private final SecurityEventLogger securityEventLogger;
    
    @Value("${spring.jwt.access-expiration-ms}")
    private Long accessTokenExpirationMs;
    
    @Value("${spring.jwt.refresh-expiration-ms}")
    private Long refreshTokenExpirationMs;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        String email = principal.getUsername();
        String role = principal.getAuthorities().iterator().next().getAuthority();
        Boolean isNewUser = principal.isNewUser();
        
        // JWT 토큰 생성
        String accessToken = jwtUtil.createJwt(JWT_CATEGORY_ACCESS, email, role, accessTokenExpirationMs);
        String refreshToken = jwtUtil.createJwt(JWT_CATEGORY_REFRESH, email, role, refreshTokenExpirationMs);
        
        // 쿠키 설정
        setTokenCookies(response, accessToken, refreshToken);
        
        // 보안 이벤트 로깅
        String ipAddress = securityEventLogger.getClientIpAddress(request);
        String userAgent = securityEventLogger.getUserAgent(request);
        String provider = principal.getUser().getProvider().getProvider();
        
        securityEventLogger.logOAuth2LoginSuccess(email, provider, ipAddress, userAgent);
        
        if (isNewUser) {
            log.info("New OAuth2 user registered: {} (provider: {})", email, provider);
            responseUtil.sendSuccessResponse(response, "소셜 로그인 성공! 추가 정보를 입력해주세요.", null);
        } else {
            log.info("Existing OAuth2 user logged in: {} (provider: {})", email, provider);
            responseUtil.sendSuccessResponse(response, "소셜 로그인 성공!", null);
        }
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
}
