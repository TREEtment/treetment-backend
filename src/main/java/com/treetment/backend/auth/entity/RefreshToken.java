package com.treetment.backend.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    private String email;
    
    @Column(nullable = false)
    private String refreshToken;
    
    @Column(nullable = false)
    private Long expiresAt;
    
    @Builder
    public RefreshToken(String email, String refreshToken, Long expiresAt) {
        this.email = email;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }
    
    public void updateToken(String newRefreshToken, Long newExpiresAt) {
        this.refreshToken = newRefreshToken;
        this.expiresAt = newExpiresAt;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > this.expiresAt;
    }
}
