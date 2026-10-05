package com.rentms.controller;

import com.rentms.dto.auth.LoginRequest;
import com.rentms.dto.auth.LoginResponse;
import com.rentms.dto.auth.RefreshTokenRequest;
import com.rentms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for mobile number: {}", request.getMobileNumber());
        LoginResponse response = authService.login(request);
        log.info("Login successful for mobile number: {}", request.getMobileNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request, @AuthenticationPrincipal com.rentms.entity.User user) {
        log.info("Token refresh request received for user: {}", user.getMobileNumber());
        LoginResponse response = authService.refreshToken(request, user.getMobileNumber());
        log.info("Token refresh successful for user: {}", user.getMobileNumber());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Auth controller is working");
    }
}