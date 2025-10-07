package com.treetment.backend.auth.domain;

public enum PROVIDER {
    LOCAL("local"),
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");
    
    private final String provider;
    
    PROVIDER(String provider) {
        this.provider = provider;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public static PROVIDER fromString(String string) {
        for (PROVIDER provider : PROVIDER.values()) {
            if (provider.provider.equalsIgnoreCase(string)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Invalid provider value: " + string);
    }
}
