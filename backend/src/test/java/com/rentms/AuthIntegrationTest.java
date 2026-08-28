package com.rentms;

import com.rentms.dto.auth.LoginRequest;
import com.rentms.dto.auth.LoginResponse;
import com.rentms.dto.health.HealthResponse;
import com.rentms.entity.User;
import com.rentms.repository.UserRepository;
import com.rentms.security.JwtService;
import com.rentms.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        userRepository.deleteAll();
    }

    @Test
    void healthEndpoint_shouldReturnUp() {
        ResponseEntity<HealthResponse> response = restTemplate.getForEntity(baseUrl + "/health", HealthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("UP");
        assertThat(response.getBody().getService()).isEqualTo("rent-management-backend");
    }

    @Test
    void login_shouldSucceedWithValidCredentials() {
        User admin = createAdminUser();
        userRepository.save(admin);

        LoginRequest request = new LoginRequest();
        request.setMobileNumber("9876543210");
        request.setPassword("Admin@123");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(baseUrl + "/auth/login", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().getUser().getMobileNumber()).isEqualTo("9876543210");
        assertThat(response.getBody().getUser().getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void login_shouldFailWithInvalidPassword() {
        User admin = createAdminUser();
        userRepository.save(admin);

        LoginRequest request = new LoginRequest();
        request.setMobileNumber("9876543210");
        request.setPassword("WrongPassword");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_shouldFailWithInvalidMobileNumber() {
        User admin = createAdminUser();
        userRepository.save(admin);

        LoginRequest request = new LoginRequest();
        request.setMobileNumber("9999999999");
        request.setPassword("Admin@123");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_shouldFailForInactiveUser() {
        User admin = createAdminUser();
        admin.setStatus(User.Status.INACTIVE);
        userRepository.save(admin);

        LoginRequest request = new LoginRequest();
        request.setMobileNumber("9876543210");
        request.setPassword("Admin@123");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void adminEndpoint_shouldRequireAdminRole() {
        User admin = createAdminUser();
        userRepository.save(admin);

        LoginRequest request = new LoginRequest();
        request.setMobileNumber("9876543210");
        request.setPassword("Admin@123");

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(baseUrl + "/auth/login", request, LoginResponse.class);
        String token = loginResponse.getBody().getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/admin/dashboard", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void jwtToken_shouldBeValidForAuthenticatedRequests() {
        User admin = createAdminUser();
        userRepository.save(admin);

        String token = jwtService.generateToken(admin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/admin/dashboard", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private User createAdminUser() {
        return User.builder()
                .name("System Admin")
                .mobileNumber("9876543210")
                .email("admin@rentms.local")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();
    }
}