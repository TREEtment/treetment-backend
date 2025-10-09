package com.treetment.backend.auth.dto;

import com.treetment.backend.auth.domain.PROVIDER;
import com.treetment.backend.auth.domain.ROLE;
import com.treetment.backend.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String email;
    private String nickname;
    private LocalDate birthDate;
    private ROLE role;
    private PROVIDER provider;
    private String profileImageUrl;
    private Boolean isActive;
    private Boolean marketingAgreement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .role(user.getRole())
                .provider(user.getProvider())
                .profileImageUrl(user.getProfileImageUrl())
                .isActive(user.getIsActive())
                .marketingAgreement(user.getMarketingAgreement())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
