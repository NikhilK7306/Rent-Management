package com.rentms.security;

import com.rentms.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    // Hardcoded fallback secret to ensure consistency across environments
    private static final String HARDCODED_SECRET = "dev-secret-key-min-256-bits-for-hmac-sha256-change-in-production";

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        System.err.println("=== JWT SERVICE INIT ===");
        System.err.println("JWT Secret from properties: [" + secret + "]");
        System.err.println("JWT Secret length: " + (secret != null ? secret.length() : "null"));
        
        // Use hardcoded secret as fallback if properties secret is null/empty
        String effectiveSecret = (secret != null && !secret.isBlank()) ? secret : HARDCODED_SECRET;
        System.err.println("Effective JWT Secret: [" + effectiveSecret + "]");
        System.err.println("Effective JWT Secret length: " + effectiveSecret.length());
        
        byte[] keyBytes = effectiveSecret.getBytes(StandardCharsets.UTF_8);
        System.err.println("Key bytes length: " + keyBytes.length);
        System.err.println("First 10 bytes: " + java.util.Arrays.toString(java.util.Arrays.copyOf(keyBytes, Math.min(10, keyBytes.length))));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        System.err.println("Secret key algorithm: " + this.secretKey.getAlgorithm());
        System.err.println("Secret key format: " + this.secretKey.getFormat());
        System.err.println("Secret key encoded length: " + this.secretKey.getEncoded().length);
        System.err.println("=== JWT SERVICE INIT END ===");
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(user.getMobileNumber())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String extractMobileNumber(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, User user) {
        try {
            String mobileNumber = extractMobileNumber(token);
            return mobileNumber.equals(user.getMobileNumber()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}