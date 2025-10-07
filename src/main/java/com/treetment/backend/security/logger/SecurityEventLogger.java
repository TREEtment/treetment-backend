package com.treetment.backend.security.logger;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SecurityEventLogger {
    
    public void logLoginSuccess(String email, String ipAddress, String userAgent) {
        log.info("LOGIN_SUCCESS - Email: {}, IP: {}, UserAgent: {}, Time: {}", 
                email, ipAddress, userAgent, LocalDateTime.now());
    }
    
    public void logLoginFailure(String email, String ipAddress, String userAgent, String reason) {
        log.warn("LOGIN_FAILURE - Email: {}, IP: {}, UserAgent: {}, Reason: {}, Time: {}", 
                email, ipAddress, userAgent, reason, LocalDateTime.now());
    }
    
    public void logOAuth2LoginSuccess(String email, String provider, String ipAddress, String userAgent) {
        log.info("OAUTH2_LOGIN_SUCCESS - Email: {}, Provider: {}, IP: {}, UserAgent: {}, Time: {}", 
                email, provider, ipAddress, userAgent, LocalDateTime.now());
    }
    
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
