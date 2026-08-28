package com.rentms.controller;

import com.rentms.dto.auth.LoginRequest;
import com.rentms.dto.auth.LoginResponse;
import com.rentms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Auth controller is working");
    }
}