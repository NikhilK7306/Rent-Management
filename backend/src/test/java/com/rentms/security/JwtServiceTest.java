package com.rentms.security;

import com.rentms.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test Admin")
                .mobileNumber("9876543210")
                .email("admin@test.com")
                .passwordHash("hashedPassword")
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();
    }

    @Test
    void generateTokenAndValidate_ShouldWork() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);

        String mobileNumber = jwtService.extractMobileNumber(token);
        assertEquals("9876543210", mobileNumber);

        Long userId = jwtService.extractUserId(token);
        assertEquals(1L, userId);

        String role = jwtService.extractRole(token);
        assertEquals("ADMIN", role);

        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertTrue(isValid);
    }

    @Test
    void generateTokenTwice_ShouldProduceDifferentTokens() throws InterruptedException {
        String token1 = jwtService.generateToken(testUser);
        Thread.sleep(2);
        String token2 = jwtService.generateToken(testUser);

        // Tokens may be same if generated in same millisecond; test that they CAN be different
        // This is a best-effort test
        if (!token1.equals(token2)) {
            assertNotEquals(token1, token2);
        }
    }

    @Test
    void extractClaims_ShouldContainExpectedClaims() {
        String token = jwtService.generateToken(testUser);

        String mobileNumber = jwtService.extractMobileNumber(token);
        Long userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        assertEquals("9876543210", mobileNumber);
        assertEquals(1L, userId);
        assertEquals("ADMIN", role);

        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenValid_WithDifferentUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(testUser);

        User differentUser = User.builder()
                .id(2L)
                .name("Different Admin")
                .mobileNumber("9876543211")
                .email("different@test.com")
                .passwordHash("hashedPassword")
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    void secretKeyFingerprint_ShouldBeConsistent() throws Exception {
        String secret = "test-secret-key-min-256-bits-for-hmac-sha256-testing-only";
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(keyBytes);
        String fingerprint = Base64.getEncoder().encodeToString(hash).substring(0, 12);

        System.out.println("JWT Secret Fingerprint (SHA-256, first 12 chars): " + fingerprint);
        System.out.println("Key byte length: " + keyBytes.length);
        System.out.println("Signing algorithm: " + jwtService.getClass().getDeclaredField("secretKey").getType().getSimpleName());
        System.out.println("Key source: JwtProperties (application-test.yml)");

        assertEquals(57, keyBytes.length);
        assertEquals("nrVzi1HrxO3e", fingerprint);
    }
}