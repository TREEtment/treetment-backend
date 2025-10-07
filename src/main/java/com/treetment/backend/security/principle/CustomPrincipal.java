package com.treetment.backend.security.principle;

import com.treetment.backend.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomPrincipal implements UserDetails, OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final String socialAccessToken;
    private final boolean isNewUser;
    
    public CustomPrincipal(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
        this.socialAccessToken = null;
        this.isNewUser = false;
    }
    
    public CustomPrincipal(User user, Map<String, Object> attributes, String socialAccessToken, boolean isNewUser) {
        this.user = user;
        this.attributes = attributes;
        this.socialAccessToken = socialAccessToken;
        this.isNewUser = isNewUser;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }
    
    @Override
    public String getName() {
        return user.getEmail();
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
