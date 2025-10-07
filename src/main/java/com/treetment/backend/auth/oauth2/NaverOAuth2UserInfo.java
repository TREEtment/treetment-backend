package com.treetment.backend.auth.oauth2;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {
    
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }
    
    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null) {
            return (String) response.get("id");
        }
        return null;
    }
    
    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null) {
            return (String) response.get("email");
        }
        return null;
    }
    
    @Override
    public String getNickname() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null) {
            return (String) response.get("nickname");
        }
        return null;
    }
    
    @Override
    public String getProfileImageUrl() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null) {
            return (String) response.get("profile_image");
        }
        return null;
    }
}
