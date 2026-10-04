package com.rentms.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApplicationGenerationProvider {

    private String generationId;

    @PostConstruct
    public void init() {
        // Generate a cryptographically secure random generation ID on startup
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        this.generationId = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        System.err.println("=== APPLICATION GENERATION ID INIT ===");
        System.err.println("Generation ID: " + this.generationId);
        System.err.println("=== APPLICATION GENERATION ID INIT END ===");
    }

    public String getGenerationId() {
        return generationId;
    }
}