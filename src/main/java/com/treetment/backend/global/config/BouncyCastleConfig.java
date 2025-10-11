package com.treetment.backend.global.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class BouncyCastleConfig {
    static { Security.addProvider(new BouncyCastleProvider()); }
}