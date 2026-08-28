package com.rentms;

import com.rentms.dto.property.PropertyPageResponse;
import com.rentms.dto.property.PropertyRequest;
import com.rentms.dto.property.PropertyResponse;
import com.rentms.dto.property.PropertyStatusRequest;
import com.rentms.entity.Property;
import com.rentms.entity.User;
import com.rentms.repository.PropertyRepository;
import com.rentms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PropertyIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private String adminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        propertyRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
                .name("System Admin")
                .mobileNumber("9876543210")
                .email("admin@rentms.local")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();
        userRepository.save(admin);

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        com.rentms.dto.auth.LoginRequest loginRequest = new com.rentms.dto.auth.LoginRequest();
        loginRequest.setMobileNumber("9876543210");
        loginRequest.setPassword("Admin@123");
        HttpEntity<com.rentms.dto.auth.LoginRequest> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);

        ResponseEntity<com.rentms.dto.auth.LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login", loginEntity, com.rentms.dto.auth.LoginResponse.class);
        adminToken = loginResponse.getBody().getAccessToken();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private PropertyRequest createValidPropertyRequest(String propertyCode) {
        PropertyRequest request = new PropertyRequest();
        request.setPropertyName("House A");
        request.setPropertyCode(propertyCode);
        request.setPropertyType(Property.PropertyType.HOUSE);
        request.setAddress("Thrissur, Kerala");
        request.setDescription("Two bedroom rental house");
        request.setMonthlyRent(new BigDecimal("10000"));
        return request;
    }

    @Test
    void createProperty_shouldSucceedForAdmin() {
        PropertyRequest request = createValidPropertyRequest("PROP-001");
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<PropertyResponse> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, PropertyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPropertyName()).isEqualTo("House A");
        assertThat(response.getBody().getPropertyCode()).isEqualTo("PROP-001");
        assertThat(response.getBody().getPropertyType()).isEqualTo(Property.PropertyType.HOUSE);
        assertThat(response.getBody().getAddress()).isEqualTo("Thrissur, Kerala");
        assertThat(response.getBody().getDescription()).isEqualTo("Two bedroom rental house");
        assertThat(response.getBody().getMonthlyRent()).isEqualByComparingTo("10000");
        assertThat(response.getBody().getStatus()).isEqualTo(Property.Status.ACTIVE);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getCreatedAt()).isNotNull();
        assertThat(response.getBody().getUpdatedAt()).isNotNull();
    }

    @Test
    void createProperty_shouldFailWithoutAuthentication() {
        PropertyRequest request = createValidPropertyRequest("PROP-001");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createProperty_shouldFailForNonAdmin() {
        User nonAdmin = User.builder()
                .name("Regular User")
                .mobileNumber("9876543211")
                .email("user@rentms.local")
                .passwordHash(passwordEncoder.encode("User@123"))
                .role(User.Role.ADMIN)
                .status(User.Status.INACTIVE)
                .build();
        userRepository.save(nonAdmin);

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        com.rentms.dto.auth.LoginRequest loginRequest = new com.rentms.dto.auth.LoginRequest();
        loginRequest.setMobileNumber("9876543211");
        loginRequest.setPassword("User@123");
        HttpEntity<com.rentms.dto.auth.LoginRequest> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);

        ResponseEntity<com.rentms.dto.auth.LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login", loginEntity, com.rentms.dto.auth.LoginResponse.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createProperty_shouldFailWithEmptyPropertyName() {
        PropertyRequest request = createValidPropertyRequest("PROP-002");
        request.setPropertyName("");
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Property name is required");
    }

    @Test
    void createProperty_shouldFailWithEmptyPropertyCode() {
        PropertyRequest request = createValidPropertyRequest("PROP-003");
        request.setPropertyCode("");
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Property code is required");
    }

    @Test
    void createProperty_shouldFailWithDuplicatePropertyCode() {
        PropertyRequest request1 = createValidPropertyRequest("PROP-004");
        HttpEntity<PropertyRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/properties", entity1, PropertyResponse.class);

        PropertyRequest request2 = createValidPropertyRequest("PROP-004");
        request2.setPropertyName("House B");
        HttpEntity<PropertyRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity2, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Property code already exists");
    }

    @Test
    void createProperty_shouldFailWithZeroMonthlyRent() {
        PropertyRequest request = createValidPropertyRequest("PROP-005");
        request.setMonthlyRent(BigDecimal.ZERO);
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Monthly rent must be greater than zero");
    }

    @Test
    void createProperty_shouldFailWithNegativeMonthlyRent() {
        PropertyRequest request = createValidPropertyRequest("PROP-006");
        request.setMonthlyRent(new BigDecimal("-100"));
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllProperties_shouldReturnEmptyListInitially() {
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());

        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties", HttpMethod.GET, entity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void getAllProperties_shouldReturnCreatedProperties() {
        PropertyRequest request = createValidPropertyRequest("PROP-007");
        HttpEntity<PropertyRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<PropertyResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/properties", createEntity, PropertyResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties", HttpMethod.GET, entity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getPropertyCode()).isEqualTo("PROP-007");
    }

    @Test
    void getPropertyById_shouldReturnProperty() {
        PropertyRequest request = createValidPropertyRequest("PROP-008");
        HttpEntity<PropertyRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<PropertyResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/properties", createEntity, PropertyResponse.class);

        Long propertyId = createResponse.getBody().getId();

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> response = restTemplate.exchange(
                baseUrl + "/admin/properties/" + propertyId, HttpMethod.GET, entity, PropertyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(propertyId);
        assertThat(response.getBody().getPropertyCode()).isEqualTo("PROP-008");
    }

    @Test
    void getPropertyById_shouldReturn404ForNonExistentProperty() {
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/properties/999999", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateProperty_shouldSucceed() {
        PropertyRequest request = createValidPropertyRequest("PROP-009");
        HttpEntity<PropertyRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<PropertyResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/properties", createEntity, PropertyResponse.class);

        Long propertyId = createResponse.getBody().getId();

        PropertyRequest updateRequest = new PropertyRequest();
        updateRequest.setPropertyName("House A Updated");
        updateRequest.setPropertyCode("PROP-009-UPDATED");
        updateRequest.setPropertyType(Property.PropertyType.APARTMENT);
        updateRequest.setAddress("Kochi, Kerala");
        updateRequest.setDescription("Updated description");
        updateRequest.setMonthlyRent(new BigDecimal("15000"));

        HttpEntity<PropertyRequest> updateEntity = new HttpEntity<>(updateRequest, getAuthHeaders());
        ResponseEntity<PropertyResponse> response = restTemplate.exchange(
                baseUrl + "/admin/properties/" + propertyId, HttpMethod.PUT, updateEntity, PropertyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPropertyName()).isEqualTo("House A Updated");
        assertThat(response.getBody().getPropertyCode()).isEqualTo("PROP-009-UPDATED");
        assertThat(response.getBody().getPropertyType()).isEqualTo(Property.PropertyType.APARTMENT);
        assertThat(response.getBody().getAddress()).isEqualTo("Kochi, Kerala");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated description");
        assertThat(response.getBody().getMonthlyRent()).isEqualByComparingTo("15000");
    }

    @Test
    void updateProperty_shouldFailWithDuplicatePropertyCode() {
        PropertyRequest request1 = createValidPropertyRequest("PROP-010");
        HttpEntity<PropertyRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        ResponseEntity<PropertyResponse> response1 = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity1, PropertyResponse.class);

        PropertyRequest request2 = createValidPropertyRequest("PROP-011");
        HttpEntity<PropertyRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        ResponseEntity<PropertyResponse> response2 = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity2, PropertyResponse.class);

        Long propertyId2 = response2.getBody().getId();

        PropertyRequest updateRequest = new PropertyRequest();
        updateRequest.setPropertyName("House B");
        updateRequest.setPropertyCode("PROP-010");
        updateRequest.setPropertyType(Property.PropertyType.HOUSE);
        updateRequest.setAddress("Kochi, Kerala");
        updateRequest.setDescription("Description");
        updateRequest.setMonthlyRent(new BigDecimal("12000"));

        HttpEntity<PropertyRequest> updateEntity = new HttpEntity<>(updateRequest, getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/properties/" + propertyId2, HttpMethod.PUT, updateEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Property code already exists");
    }

    @Test
    void updatePropertyStatus_shouldDeactivateProperty() {
        PropertyRequest request = createValidPropertyRequest("PROP-012");
        HttpEntity<PropertyRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<PropertyResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/properties", createEntity, PropertyResponse.class);

        Long propertyId = createResponse.getBody().getId();
        assertThat(createResponse.getBody().getStatus()).isEqualTo(Property.Status.ACTIVE);

        PropertyStatusRequest statusRequest = new PropertyStatusRequest();
        statusRequest.setStatus(Property.Status.INACTIVE);

        webTestClient.patch()
                .uri("/api/admin/properties/" + propertyId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PropertyResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(Property.Status.INACTIVE);
                });
    }

    @Test
    void updatePropertyStatus_shouldActivateProperty() {
        PropertyRequest request = createValidPropertyRequest("PROP-013");
        HttpEntity<PropertyRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<PropertyResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/properties", createEntity, PropertyResponse.class);

        Long propertyId = createResponse.getBody().getId();

        PropertyStatusRequest deactivateRequest = new PropertyStatusRequest();
        deactivateRequest.setStatus(Property.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/properties/" + propertyId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        PropertyStatusRequest activateRequest = new PropertyStatusRequest();
        activateRequest.setStatus(Property.Status.ACTIVE);
        webTestClient.patch()
                .uri("/api/admin/properties/" + propertyId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(activateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PropertyResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(Property.Status.ACTIVE);
                });
    }

    @Test
    void searchProperties_shouldWorkByPropertyName() {
        PropertyRequest request1 = createValidPropertyRequest("PROP-014");
        request1.setPropertyName("House Alpha");
        HttpEntity<PropertyRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/properties", entity1, PropertyResponse.class);

        PropertyRequest request2 = createValidPropertyRequest("PROP-015");
        request2.setPropertyName("Shop Beta");
        HttpEntity<PropertyRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/properties", entity2, PropertyResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties?search=house", HttpMethod.GET, entity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getPropertyName()).isEqualTo("House Alpha");
    }

    @Test
    void searchProperties_shouldWorkByPropertyCode() {
        PropertyRequest request = createValidPropertyRequest("PROP-SEARCH-001");
        request.setPropertyName("Test Property");
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/properties", entity, PropertyResponse.class);

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties?search=PROP-SEARCH", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void searchProperties_shouldWorkByAddress() {
        PropertyRequest request = createValidPropertyRequest("PROP-016");
        request.setPropertyName("Property A");
        request.setAddress("Main Street, Kochi");
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/properties", entity, PropertyResponse.class);

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties?search=koch", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void filterPropertiesByStatus_shouldWork() {
        PropertyRequest request1 = createValidPropertyRequest("PROP-017");
        HttpEntity<PropertyRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        ResponseEntity<PropertyResponse> response1 = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity1, PropertyResponse.class);

        PropertyRequest request2 = createValidPropertyRequest("PROP-018");
        HttpEntity<PropertyRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        ResponseEntity<PropertyResponse> response2 = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity2, PropertyResponse.class);

        Long propertyId2 = response2.getBody().getId();

        PropertyStatusRequest deactivateRequest = new PropertyStatusRequest();
        deactivateRequest.setStatus(Property.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/properties/" + propertyId2 + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties?status=ACTIVE", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(Property.Status.ACTIVE);
    }

    @Test
    void combinedSearchAndFilter_shouldWork() {
        PropertyRequest request1 = createValidPropertyRequest("PROP-019");
        request1.setPropertyName("House Active");
        HttpEntity<PropertyRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/properties", entity1, PropertyResponse.class);

        PropertyRequest request2 = createValidPropertyRequest("PROP-020");
        request2.setPropertyName("House Inactive");
        HttpEntity<PropertyRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        ResponseEntity<PropertyResponse> response2 = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity2, PropertyResponse.class);

        Long propertyId2 = response2.getBody().getId();

        PropertyStatusRequest deactivateRequest = new PropertyStatusRequest();
        deactivateRequest.setStatus(Property.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/properties/" + propertyId2 + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties?search=house&status=ACTIVE", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getPropertyName()).isEqualTo("House Active");
        assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(Property.Status.ACTIVE);
    }

    @Test
    void pagination_shouldWork() {
        for (int i = 1; i <= 15; i++) {
            PropertyRequest request = createValidPropertyRequest("PROP-PAGE-" + String.format("%03d", i));
            HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());
            restTemplate.postForEntity(baseUrl + "/admin/properties", entity, PropertyResponse.class);
        }

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyPageResponse<PropertyResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/properties?page=0&size=5", HttpMethod.GET, entity,
                new ParameterizedTypeReference<PropertyPageResponse<PropertyResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(5);
        assertThat(response.getBody().getTotalElements()).isEqualTo(15);
        assertThat(response.getBody().getTotalPages()).isEqualTo(3);
    }
}