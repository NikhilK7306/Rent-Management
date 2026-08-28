package com.rentms.service;

import com.rentms.dto.auth.LoginRequest;
import com.rentms.dto.auth.LoginResponse;
import com.rentms.entity.User;
import com.rentms.exception.InvalidCredentialsException;
import com.rentms.exception.UserInactiveException;
import com.rentms.exception.UserNotFoundException;
import com.rentms.repository.UserRepository;
import com.rentms.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.debug("Attempting login for mobile number: {}", request.getMobileNumber());
        
        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> {
                    log.warn("User not found for mobile number: {}", request.getMobileNumber());
                    return new InvalidCredentialsException("Invalid mobile number or password");
                });

        log.debug("User found: id={}, mobileNumber={}, status={}, role={}", 
                user.getId(), user.getMobileNumber(), user.getStatus(), user.getRole());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for mobile number: {}", request.getMobileNumber());
            throw new InvalidCredentialsException("Invalid mobile number or password");
        }

        log.debug("Password matched for user: {}", user.getMobileNumber());

        if (user.getStatus() != User.Status.ACTIVE) {
            log.warn("User account not active: mobileNumber={}, status={}", 
                    request.getMobileNumber(), user.getStatus());
            throw new UserInactiveException("Account is not active. Please contact administrator.");
        }

        if (user.getRole() != User.Role.ADMIN) {
            log.warn("User does not have ADMIN role: mobileNumber={}, role={}", 
                    request.getMobileNumber(), user.getRole());
            throw new InvalidCredentialsException("Access denied. Admin role required.");
        }

        log.debug("Generating JWT token for user: {}", user.getMobileNumber());
        String token = jwtService.generateToken(user);

        log.debug("Login successful for user: {}", user.getMobileNumber());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(LoginResponse.UserDto.from(user))
                .build();
    }
}