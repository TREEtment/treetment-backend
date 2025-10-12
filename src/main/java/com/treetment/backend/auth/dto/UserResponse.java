package com.treetment.backend.auth.dto;

import com.treetment.backend.auth.domain.PROVIDER;
import com.treetment.backend.auth.domain.ROLE;
import com.treetment.backend.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String email;
    private String nickname;
    private String name;
    private ROLE role;
    private PROVIDER provider;
    private String profileImageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .role(user.getRole())
                .provider(user.getProvider())
                .profileImageUrl(user.getProfileImageUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
