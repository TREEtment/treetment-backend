package com.treetment.backend.security.filter;

import com.treetment.backend.auth.repository.UserRepository;
import com.treetment.backend.security.principle.CustomPrincipal;
import com.treetment.backend.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.treetment.backend.security.util.JwtUtil.ACCESS_TOKEN_COOKIE_NAME;
import static com.treetment.backend.security.util.JwtUtil.JWT_CATEGORY_ACCESS;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractTokenFromCookies(request, ACCESS_TOKEN_COOKIE_NAME);
        
        if (accessToken != null) {
            try {
                String category = jwtUtil.getCategory(accessToken);
                String email = jwtUtil.getEmail(accessToken);
                
                if (JWT_CATEGORY_ACCESS.equals(category)) {
                    var user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    
                    CustomPrincipal customPrincipal = new CustomPrincipal(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            customPrincipal, null, customPrincipal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
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
