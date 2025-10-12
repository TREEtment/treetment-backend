package com.treetment.backend.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    
    public static final String JWT_CATEGORY_ACCESS = "access";
    public static final String JWT_CATEGORY_REFRESH = "refresh";
    public static final String JWT_CATEGORY_TEMP = "temp";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "ACCESS-TOKEN";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH-TOKEN";
    public static final String TEMP_TOKEN_COOKIE_NAME = "TEMP-TOKEN";
    
    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String createJwt(String category, String email, String role, Long expiredMs) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusMillis(expiredMs);
        
        return Jwts.builder()
                .claim("category", category)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }
    
    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }
    
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }
    
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
