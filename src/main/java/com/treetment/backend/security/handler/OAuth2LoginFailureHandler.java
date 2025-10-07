package com.treetment.backend.security.handler;

import com.treetment.backend.global.util.ResponseUtil;
import com.treetment.backend.security.logger.SecurityEventLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    
    private final ResponseUtil responseUtil;
    private final SecurityEventLogger securityEventLogger;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException, ServletException {
        
        String ipAddress = securityEventLogger.getClientIpAddress(request);
        String userAgent = securityEventLogger.getUserAgent(request);
        
        securityEventLogger.logLoginFailure("oauth2_user", ipAddress, userAgent, exception.getMessage());
        
        log.error("OAuth2 login failed: {}", exception.getMessage(), exception);
        
        responseUtil.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, 
                "소셜 로그인에 실패했습니다: " + exception.getMessage());
    }
}
