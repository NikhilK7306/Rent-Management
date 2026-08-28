package com.rentms;

import com.rentms.dto.property.PropertyPageResponse;
import com.rentms.dto.property.PropertyRequest;
import com.rentms.dto.property.PropertyResponse;
import com.rentms.dto.property.PropertyStatusRequest;
import com.rentms.dto.tenant.TenantPageResponse;
import com.rentms.dto.tenant.TenantPropertyRequest;
import com.rentms.dto.tenant.TenantRequest;
import com.rentms.dto.tenant.TenantResponse;
import com.rentms.dto.tenant.TenantStatusRequest;
import com.rentms.entity.Property;
import com.rentms.entity.Tenant;
import com.rentms.entity.User;
import com.rentms.repository.PropertyRepository;
import com.rentms.repository.TenantRepository;
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
class TenantIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TenantRepository tenantRepository;

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
        tenantRepository.deleteAll();
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

    private TenantRequest createValidTenantRequest(String mobileNumber) {
        TenantRequest request = new TenantRequest();
        request.setFullName("Test Tenant");
        request.setMobileNumber(mobileNumber);
        request.setEmail("tenant@example.com");
        request.setAddress("Test Address, City");
        return request;
    }

    private PropertyResponse createProperty(String propertyCode) {
        PropertyRequest request = new PropertyRequest();
        request.setPropertyName("Test Property");
        request.setPropertyCode(propertyCode);
        request.setPropertyType(Property.PropertyType.HOUSE);
        request.setAddress("Property Address");
        request.setDescription("Test property description");
        request.setMonthlyRent(new BigDecimal("10000"));
        HttpEntity<PropertyRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<PropertyResponse> response = restTemplate.postForEntity(
                baseUrl + "/admin/properties", entity, PropertyResponse.class);
        return response.getBody();
    }

    @Test
    void createTenant_shouldSucceedForAdmin() {
        TenantRequest request = createValidTenantRequest("9123456789");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<TenantResponse> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("Test Tenant");
        assertThat(response.getBody().getMobileNumber()).isEqualTo("9123456789");
        assertThat(response.getBody().getEmail()).isEqualTo("tenant@example.com");
        assertThat(response.getBody().getAddress()).isEqualTo("Test Address, City");
        assertThat(response.getBody().getStatus()).isEqualTo(Tenant.Status.ACTIVE);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getCreatedAt()).isNotNull();
        assertThat(response.getBody().getUpdatedAt()).isNotNull();
        assertThat(response.getBody().getProperty()).isNull();
    }

    @Test
    void createTenant_shouldFailWithoutAuthentication() {
        TenantRequest request = createValidTenantRequest("9123456789");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createTenant_shouldFailWithEmptyFullName() {
        TenantRequest request = createValidTenantRequest("9123456790");
        request.setFullName("");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Full name is required");
    }

    @Test
    void createTenant_shouldFailWithEmptyMobileNumber() {
        TenantRequest request = createValidTenantRequest("9123456791");
        request.setMobileNumber("");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String body = response.getBody();
        System.out.println("Response body: " + body);
        // Multiple validations can run; accept any valid error message for empty mobile number
        assertThat(body).as("Response body should contain error message")
                .satisfiesAnyOf(
                        b -> assertThat(b).contains("Mobile number is required"),
                        b -> assertThat(b).contains("Mobile number must be between 10 and 15 characters"),
                        b -> assertThat(b).contains("Mobile number must contain only digits")
                );
    }

    @Test
    void createTenant_shouldFailWithInvalidMobileNumber() {
        TenantRequest request = createValidTenantRequest("9123456792");
        request.setMobileNumber("abc123def456"); // 12 chars - @Size passes, @Pattern fails
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Mobile number must contain only digits");
    }

    @Test
    void createTenant_shouldFailWithShortMobileNumber() {
        TenantRequest request = createValidTenantRequest("9123456793");
        request.setMobileNumber("12345");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Mobile number must be between 10 and 15 characters");
    }

    @Test
    void createTenant_shouldFailWithInvalidEmail() {
        TenantRequest request = createValidTenantRequest("9123456794");
        request.setEmail("invalid-email");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid email format");
    }

    @Test
    void createTenant_shouldFailWithDuplicateMobileNumber() {
        TenantRequest request1 = createValidTenantRequest("9123456795");
        HttpEntity<TenantRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/tenants", entity1, TenantResponse.class);

        TenantRequest request2 = createValidTenantRequest("9123456795");
        request2.setFullName("Another Tenant");
        HttpEntity<TenantRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity2, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Mobile number already exists");
    }

    @Test
    void getAllTenants_shouldReturnEmptyListInitially() {
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());

        ResponseEntity<TenantPageResponse<TenantResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/tenants", HttpMethod.GET, entity,
                new ParameterizedTypeReference<TenantPageResponse<TenantResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void getAllTenants_shouldReturnCreatedTenants() {
        TenantRequest request = createValidTenantRequest("9123456796");
        HttpEntity<TenantRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/tenants", createEntity, TenantResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantPageResponse<TenantResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/tenants", HttpMethod.GET, entity,
                new ParameterizedTypeReference<TenantPageResponse<TenantResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getMobileNumber()).isEqualTo("9123456796");
    }

    @Test
    void getTenantById_shouldReturnTenant() {
        TenantRequest request = createValidTenantRequest("9123456797");
        HttpEntity<TenantRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", createEntity, TenantResponse.class);

        Long tenantId = createResponse.getBody().getId();

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantResponse> response = restTemplate.exchange(
                baseUrl + "/admin/tenants/" + tenantId, HttpMethod.GET, entity, TenantResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(tenantId);
        assertThat(response.getBody().getMobileNumber()).isEqualTo("9123456797");
    }

    @Test
    void getTenantById_shouldReturn404ForNonExistentTenant() {
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/tenants/999999", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTenant_shouldSucceed() {
        TenantRequest request = createValidTenantRequest("9123456798");
        HttpEntity<TenantRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", createEntity, TenantResponse.class);

        Long tenantId = createResponse.getBody().getId();

        TenantRequest updateRequest = new TenantRequest();
        updateRequest.setFullName("Updated Tenant");
        updateRequest.setMobileNumber("9123456799");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAddress("Updated Address");

        HttpEntity<TenantRequest> updateEntity = new HttpEntity<>(updateRequest, getAuthHeaders());
        ResponseEntity<TenantResponse> response = restTemplate.exchange(
                baseUrl + "/admin/tenants/" + tenantId, HttpMethod.PUT, updateEntity, TenantResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("Updated Tenant");
        assertThat(response.getBody().getMobileNumber()).isEqualTo("9123456799");
        assertThat(response.getBody().getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getBody().getAddress()).isEqualTo("Updated Address");
    }

    @Test
    void updateTenant_shouldFailWithDuplicateMobileNumber() {
        TenantRequest request1 = createValidTenantRequest("9123456800");
        HttpEntity<TenantRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        ResponseEntity<TenantResponse> response1 = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity1, TenantResponse.class);

        TenantRequest request2 = createValidTenantRequest("9123456801");
        HttpEntity<TenantRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        ResponseEntity<TenantResponse> response2 = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity2, TenantResponse.class);

        Long tenantId2 = response2.getBody().getId();

        TenantRequest updateRequest = new TenantRequest();
        updateRequest.setFullName("Tenant 2 Updated");
        updateRequest.setMobileNumber("9123456800");
        updateRequest.setEmail("tenant2@example.com");
        updateRequest.setAddress("Address 2");

        HttpEntity<TenantRequest> updateEntity = new HttpEntity<>(updateRequest, getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/tenants/" + tenantId2, HttpMethod.PUT, updateEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Mobile number already exists");
    }

    @Test
    void updateTenantStatus_shouldDeactivateTenant() {
        TenantRequest request = createValidTenantRequest("9123456802");
        HttpEntity<TenantRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", createEntity, TenantResponse.class);

        Long tenantId = createResponse.getBody().getId();
        assertThat(createResponse.getBody().getStatus()).isEqualTo(Tenant.Status.ACTIVE);

        TenantStatusRequest statusRequest = new TenantStatusRequest();
        statusRequest.setStatus(Tenant.Status.INACTIVE);

        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TenantResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(Tenant.Status.INACTIVE);
                });
    }

    @Test
    void updateTenantStatus_shouldActivateTenant() {
        TenantRequest request = createValidTenantRequest("9123456803");
        HttpEntity<TenantRequest> createEntity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", createEntity, TenantResponse.class);

        Long tenantId = createResponse.getBody().getId();

        TenantStatusRequest deactivateRequest = new TenantStatusRequest();
        deactivateRequest.setStatus(Tenant.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        TenantStatusRequest activateRequest = new TenantStatusRequest();
        activateRequest.setStatus(Tenant.Status.ACTIVE);
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(activateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TenantResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(Tenant.Status.ACTIVE);
                });
    }

    @Test
    void assignProperty_shouldSucceed() {
        TenantRequest tenantRequest = createValidTenantRequest("9123456804");
        HttpEntity<TenantRequest> tenantEntity = new HttpEntity<>(tenantRequest, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", tenantEntity, TenantResponse.class);

        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-001");

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());

        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TenantResponse.class)
                .value(response -> {
                    assertThat(response.getProperty()).isNotNull();
                    assertThat(response.getProperty().getId()).isEqualTo(property.getId());
                    assertThat(response.getProperty().getPropertyCode()).isEqualTo("PROP-TENANT-001");
                });

        // Verify property response also shows tenant
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> propResponse = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property.getId(), HttpMethod.GET, entity, PropertyResponse.class);
        assertThat(propResponse.getBody().getTenant()).isNotNull();
        assertThat(propResponse.getBody().getTenant().getId()).isEqualTo(tenantId);
    }

    @Test
    void assignProperty_shouldFailForNonExistentProperty() {
        TenantRequest request = createValidTenantRequest("9123456805");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);

        Long tenantId = tenantResponse.getBody().getId();

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(999999L);

        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void assignProperty_shouldFailForInactiveProperty() {
        TenantRequest request = createValidTenantRequest("9123456806");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);

        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-002");

        // Deactivate property
        PropertyStatusRequest deactivateRequest = new PropertyStatusRequest();
        deactivateRequest.setStatus(Property.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/properties/" + property.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());

        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cannot assign tenant to inactive property");
    }

    @Test
    void assignProperty_shouldFailForInactiveTenant() {
        TenantRequest request = createValidTenantRequest("9123456807");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);

        Long tenantId = tenantResponse.getBody().getId();

        // Deactivate tenant
        TenantStatusRequest deactivateRequest = new TenantStatusRequest();
        deactivateRequest.setStatus(Tenant.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        PropertyResponse property = createProperty("PROP-TENANT-003");

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());

        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cannot assign property to inactive tenant");
    }

    @Test
    void assignProperty_shouldFailForPropertyWithActiveTenant() {
        // Create first tenant and assign to property
        TenantRequest request1 = createValidTenantRequest("9123456808");
        HttpEntity<TenantRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse1 = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity1, TenantResponse.class);
        Long tenantId1 = tenantResponse1.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-004");

        TenantPropertyRequest assignRequest1 = new TenantPropertyRequest();
        assignRequest1.setPropertyId(property.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId1 + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest1)
                .exchange()
                .expectStatus().isOk();

        // Create second tenant
        TenantRequest request2 = createValidTenantRequest("9123456809");
        HttpEntity<TenantRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse2 = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity2, TenantResponse.class);
        Long tenantId2 = tenantResponse2.getBody().getId();

        // Try to assign second tenant to same property
        TenantPropertyRequest assignRequest2 = new TenantPropertyRequest();
        assignRequest2.setPropertyId(property.getId());

        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId2 + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest2)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("already assigned to another active tenant"));
    }

    @Test
    void unassignProperty_shouldSucceed() {
        TenantRequest request = createValidTenantRequest("9123456810");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);
        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-005");

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isOk();

        // Unassign
        webTestClient.delete()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TenantResponse.class)
                .value(response -> {
                    assertThat(response.getProperty()).isNull();
                });

        // Verify property also shows no tenant
        HttpEntity<Void> propEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> propResponse = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property.getId(), HttpMethod.GET, propEntity, PropertyResponse.class);
        assertThat(propResponse.getBody().getTenant()).isNull();
    }

    @Test
    void changeProperty_shouldSucceed() {
        TenantRequest request = createValidTenantRequest("9123456811");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);
        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property1 = createProperty("PROP-TENANT-006");
        PropertyResponse property2 = createProperty("PROP-TENANT-007");

        // Assign to property1
        TenantPropertyRequest assignRequest1 = new TenantPropertyRequest();
        assignRequest1.setPropertyId(property1.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest1)
                .exchange()
                .expectStatus().isOk();

        // Change to property2
        TenantPropertyRequest assignRequest2 = new TenantPropertyRequest();
        assignRequest2.setPropertyId(property2.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest2)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TenantResponse.class)
                .value(response -> {
                    assertThat(response.getProperty()).isNotNull();
                    assertThat(response.getProperty().getId()).isEqualTo(property2.getId());
                });

        // Verify property1 is now unassigned
        HttpEntity<Void> propEntity1 = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> propResponse1 = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property1.getId(), HttpMethod.GET, propEntity1, PropertyResponse.class);
        assertThat(propResponse1.getBody().getTenant()).isNull();

        // Verify property2 has the tenant
        HttpEntity<Void> propEntity2 = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> propResponse2 = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property2.getId(), HttpMethod.GET, propEntity2, PropertyResponse.class);
        assertThat(propResponse2.getBody().getTenant()).isNotNull();
        assertThat(propResponse2.getBody().getTenant().getId()).isEqualTo(tenantId);
    }

    @Test
    void searchTenants_shouldWorkByName() {
        TenantRequest request1 = createValidTenantRequest("9123456812");
        request1.setFullName("John Doe");
        HttpEntity<TenantRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/tenants", entity1, TenantResponse.class);

        TenantRequest request2 = createValidTenantRequest("9123456813");
        request2.setFullName("Jane Smith");
        HttpEntity<TenantRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/tenants", entity2, TenantResponse.class);

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantPageResponse<TenantResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/tenants?search=john", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<TenantPageResponse<TenantResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void searchTenants_shouldWorkByMobileNumber() {
        TenantRequest request = createValidTenantRequest("9123456814");
        request.setFullName("Search Tenant");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/tenants", entity, TenantResponse.class);

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantPageResponse<TenantResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/tenants?search=9123456814", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<TenantPageResponse<TenantResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void searchTenants_shouldWorkByEmail() {
        TenantRequest request = createValidTenantRequest("9123456815");
        request.setEmail("search@example.com");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        restTemplate.postForEntity(baseUrl + "/admin/tenants", entity, TenantResponse.class);

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantPageResponse<TenantResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/tenants?search=search@", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<TenantPageResponse<TenantResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void filterTenantsByStatus_shouldWork() {
        TenantRequest request1 = createValidTenantRequest("9123456816");
        HttpEntity<TenantRequest> entity1 = new HttpEntity<>(request1, getAuthHeaders());
        ResponseEntity<TenantResponse> response1 = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity1, TenantResponse.class);

        TenantRequest request2 = createValidTenantRequest("9123456817");
        HttpEntity<TenantRequest> entity2 = new HttpEntity<>(request2, getAuthHeaders());
        ResponseEntity<TenantResponse> response2 = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity2, TenantResponse.class);

        Long tenantId2 = response2.getBody().getId();

        // Deactivate second tenant
        TenantStatusRequest deactivateRequest = new TenantStatusRequest();
        deactivateRequest.setStatus(Tenant.Status.INACTIVE);
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId2 + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateRequest)
                .exchange()
                .expectStatus().isOk();

        HttpEntity<Void> searchEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantPageResponse<TenantResponse>> response = restTemplate.exchange(
                baseUrl + "/admin/tenants?status=ACTIVE", HttpMethod.GET, searchEntity,
                new ParameterizedTypeReference<TenantPageResponse<TenantResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(Tenant.Status.ACTIVE);
    }

    @Test
    void tenantResponse_shouldContainPropertyInfoWhenAssigned() {
        TenantRequest request = createValidTenantRequest("9123456818");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);
        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-008");

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TenantResponse.class)
                .value(response -> {
                    assertThat(response.getProperty()).isNotNull();
                    assertThat(response.getProperty().getId()).isEqualTo(property.getId());
                    assertThat(response.getProperty().getPropertyCode()).isEqualTo("PROP-TENANT-008");
                    assertThat(response.getProperty().getPropertyName()).isEqualTo("Test Property");
                });
    }

    @Test
    void tenantResponse_shouldHaveNullPropertyWhenUnassigned() {
        TenantRequest request = createValidTenantRequest("9123456819");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> response = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);

        assertThat(response.getBody().getProperty()).isNull();
    }

    @Test
    void propertyResponse_shouldContainTenantInfoWhenAssigned() {
        TenantRequest request = createValidTenantRequest("9123456820");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);
        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-009");

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isOk();

        HttpEntity<Void> propEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> propResponse = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property.getId(), HttpMethod.GET, propEntity, PropertyResponse.class);

        assertThat(propResponse.getBody().getTenant()).isNotNull();
        assertThat(propResponse.getBody().getTenant().getId()).isEqualTo(tenantId);
        assertThat(propResponse.getBody().getTenant().getFullName()).isEqualTo("Test Tenant");
        assertThat(propResponse.getBody().getTenant().getMobileNumber()).isEqualTo("9123456820");
    }

    @Test
    void propertyResponse_shouldHaveNullTenantWhenUnassigned() {
        PropertyResponse property = createProperty("PROP-TENANT-010");

        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> response = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property.getId(), HttpMethod.GET, entity, PropertyResponse.class);

        assertThat(response.getBody().getTenant()).isNull();
    }

    @Test
    void noCircularJsonSerialization() {
        TenantRequest request = createValidTenantRequest("9123456821");
        HttpEntity<TenantRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        ResponseEntity<TenantResponse> tenantResponse = restTemplate.postForEntity(
                baseUrl + "/admin/tenants", entity, TenantResponse.class);
        Long tenantId = tenantResponse.getBody().getId();

        PropertyResponse property = createProperty("PROP-TENANT-011");

        TenantPropertyRequest assignRequest = new TenantPropertyRequest();
        assignRequest.setPropertyId(property.getId());
        webTestClient.patch()
                .uri("/api/admin/tenants/" + tenantId + "/property")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assignRequest)
                .exchange()
                .expectStatus().isOk();

        // Get tenant - should have property but property should not have nested tenant
        HttpEntity<Void> tenantEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<TenantResponse> getTenantResponse = restTemplate.exchange(
                baseUrl + "/admin/tenants/" + tenantId, HttpMethod.GET, tenantEntity, TenantResponse.class);

        assertThat(getTenantResponse.getBody().getProperty()).isNotNull();
        assertThat(getTenantResponse.getBody().getProperty().getPropertyCode()).isEqualTo("PROP-TENANT-011");

        // Get property - should have tenant but tenant should not have nested property
        HttpEntity<Void> propEntity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<PropertyResponse> getPropResponse = restTemplate.exchange(
                baseUrl + "/admin/properties/" + property.getId(), HttpMethod.GET, propEntity, PropertyResponse.class);

        assertThat(getPropResponse.getBody().getTenant()).isNotNull();
        assertThat(getPropResponse.getBody().getTenant().getMobileNumber()).isEqualTo("9123456821");
    }
}