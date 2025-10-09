package com.treetment.backend.auth.entity;

import com.treetment.backend.auth.domain.PROVIDER;
import com.treetment.backend.auth.domain.ROLE;
import com.treetment.backend.global.entity.Core;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_users")
public class User extends Core {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    
    @Column(unique = true)
    private String nickname;
    
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ROLE role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PROVIDER provider;
    
    private String providerId;
    private String socialAccessToken;
    private String socialRefreshToken;
    private LocalDateTime accessTokenExpiresAt;
    
    private String profileImageUrl;
    
    @Column(nullable = false)
    private Boolean isActive = false;
    
    @Column(nullable = false)
    private Boolean marketingAgreement = false;
    
    @Builder
    public User(String email, String password, String nickname, LocalDate birthDate, 
                ROLE role, PROVIDER provider, String providerId, String socialAccessToken, 
                String socialRefreshToken, LocalDateTime accessTokenExpiresAt, 
                String profileImageUrl, Boolean isActive, Boolean marketingAgreement) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.socialAccessToken = socialAccessToken;
        this.socialRefreshToken = socialRefreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.profileImageUrl = profileImageUrl;
        this.isActive = isActive;
        this.marketingAgreement = marketingAgreement;
    }
    
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }
    
    public void updatePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(newPassword);
    }
    
    public void updateProfile(String nickname, LocalDate birthDate, Boolean marketingAgreement) {
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.marketingAgreement = marketingAgreement;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void updateSocialTokens(String socialAccessToken, String socialRefreshToken, LocalDateTime accessTokenExpiresAt) {
        this.socialAccessToken = socialAccessToken;
        this.socialRefreshToken = socialRefreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
}
