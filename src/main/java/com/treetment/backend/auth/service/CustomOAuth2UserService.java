package com.treetment.backend.auth.service;

import com.treetment.backend.auth.domain.PROVIDER;
import com.treetment.backend.auth.domain.ROLE;
import com.treetment.backend.user.entity.User;
import com.treetment.backend.auth.oauth2.OAuth2UserInfo;
import com.treetment.backend.auth.oauth2.OAuth2UserInfoFactory;
import com.treetment.backend.user.repository.UserRepository;
import com.treetment.backend.security.principle.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String socialAccessToken = userRequest.getAccessToken().getTokenValue();
        
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();
        String providerId = userInfo.getProviderId();
        String profileImageUrl = userInfo.getProfileImageUrl();
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        Boolean isNewUser = false;
        
        if (user == null) {
            isNewUser = true;
            user = User.builder()
                    .email(email)
                    .nickname(nickname != null ? nickname : email.split("@")[0])
                    .name(nickname != null ? nickname : email.split("@")[0]) // OAuth2에서는 name을 nickname과 동일하게 설정
                    .role(ROLE.ROLE_TEMP)
                    .provider(PROVIDER.fromString(registrationId))
                    .providerId(providerId)
                    .socialAccessToken(socialAccessToken)
                    .accessTokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .profileImageUrl(profileImageUrl)
                    .isActive(false)
                    .build();
            } else {
                // 기존 사용자의 소셜 토큰 정보 업데이트
                user.updateSocialTokens(socialAccessToken, null, LocalDateTime.now().plusHours(1));
                if (profileImageUrl != null) {
                    // 프로필 이미지 URL 업데이트는 별도 메서드로 처리
                    // user.setProfileImageUrl(profileImageUrl);
                }
            }
        
        if (user.getRole() == ROLE.ROLE_TEMP) {
            isNewUser = true;
        }
        
        userRepository.save(user);
        
        log.info("OAuth2 user loaded: {} (provider: {}, isNew: {})", email, registrationId, isNewUser);
        
        return new CustomPrincipal(user, oauth2User.getAttributes(), socialAccessToken, isNewUser);
    }
}
