package com.insurance.claimapi.controller;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.*;
import com.insurance.claimapi.repository.*;
import com.insurance.claimapi.security.JwtTokenProvider;
import com.insurance.claimapi.service.NotificationServiceClient;
import com.insurance.claimapi.service.PolicyServiceClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClaimControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClaimRepository claimRepository;
    @Autowired private PolicyRepository policyRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockBean private PolicyServiceClient policyServiceClient;
    @MockBean private NotificationServiceClient notificationServiceClient;

    private User customerUser;
    private User adjusterUser;
    private Role customerRole;
    private Role adjusterRole;
    private Policy testPolicy;

    @BeforeEach
    void setUp() {
        customerRole = roleRepository.findByName("CUSTOMER").orElseGet(() ->
                roleRepository.save(Role.builder().name("CUSTOMER").description("Customer").build()));

        adjusterRole = roleRepository.findByName("ADJUSTER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ADJUSTER").description("Adjuster").build()));

        // 密码用 BCrypt 编码，登录时需要明文 "password123"
        customerUser = userRepository.save(User.builder()
                .username("customer1").password(passwordEncoder.encode("password123")).email("customer@test.com")
                .fullName("Customer One").role(customerRole).enabled(true).build());

        adjusterUser = userRepository.save(User.builder()
                .username("adjuster1").password(passwordEncoder.encode("password123")).email("adjuster@test.com")
                .fullName("Adjuster One").role(adjusterRole).enabled(true).build());

        testPolicy = policyRepository.save(Policy.builder()
                .policyNumber("POL-TEST-001")
                .customer(customerUser)
                .customerName("Customer One")
                .policyType("AUTO")
                .policyStatus("ACTIVE")
                .coverage(new BigDecimal("100000"))
                .premium(new BigDecimal("500"))
                .startDate(LocalDate.now().minusYears(1))
                .expiryDate(LocalDate.now().plusYears(1))
                .build());
    }

    @AfterEach
    void tearDown() {
        claimRepository.deleteAll();
        policyRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * 辅助方法：用真实 JWT token 认证
     */
    private String getCustomerToken() {
        return jwtTokenProvider.generateToken(customerUser);
    }

    private String getAdjusterToken() {
        return jwtTokenProvider.generateToken(adjusterUser);
    }

    @Test
    @DisplayName("POST /claims - unauthenticated returns 403")
    void submitClaim_unauthenticated() throws Exception {
        mockMvc.perform(post("/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /claims - customer submits claim successfully")
    void submitClaim_success() throws Exception {
        PolicyDto mockPolicy = PolicyDto.builder()
                .policyNumber("POL-TEST-001").active(true)
                .expiryDate(LocalDate.now().plusYears(1)).build();
        when(policyServiceClient.getPolicy("POL-TEST-001")).thenReturn(mockPolicy);

        String requestBody = "{\"policyNumber\": \"POL-TEST-001\", \"claimType\": \"ACCIDENT\", \"incidentDate\": \"" + LocalDate.now().minusDays(1).toString() + "\", \"description\": \"Car accident on highway ramp\", \"amount\": 5000.00}";

        mockMvc.perform(post("/claims")
                        .header("Authorization", "Bearer " + getCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber", startsWith("CLM-")))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("POST /claims - validation errors return 400")
    void submitClaim_validationError() throws Exception {
        String requestBody = "{\"policyNumber\": \"\", \"claimType\": \"\", \"incidentDate\": null, \"description\": \"short\", \"amount\": -100}";

        mockMvc.perform(post("/claims")
                        .header("Authorization", "Bearer " + getCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("GET /claims/my - returns only user's claims")
    void getMyClaims_success() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-001").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test")
                .amount(new BigDecimal("1000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        claimRepository.save(claim);

        mockMvc.perform(get("/claims/my")
                        .header("Authorization", "Bearer " + getCustomerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-TEST-001"));
    }

    @Test
    @DisplayName("GET /claims/{id} - returns claim details")
    void getClaimById_success() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-002").claimType("THEFT")
                .incidentDate(LocalDate.now().minusDays(2)).description("Stolen items")
                .amount(new BigDecimal("3000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        mockMvc.perform(get("/claims/{id}", saved.getId())
                        .header("Authorization", "Bearer " + getCustomerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-TEST-002"));
    }

    @Test
    @DisplayName("GET /claims/{id} - not found returns 404")
    void getClaimById_notFound() throws Exception {
        mockMvc.perform(get("/claims/{id}", 99999)
                        .header("Authorization", "Bearer " + getCustomerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /claims/{id}/status - customer cannot approve claims (403)")
    void updateStatus_customerForbidden() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-003").claimType("ACCIDENT")
                .incidentDate(LocalDate.now()).description("Test")
                .amount(new BigDecimal("500")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        String body = "{\"status\": \"APPROVED\", \"reviewNotes\": \"Approved\"}";

        mockMvc.perform(put("/claims/{id}/status", saved.getId())
                        .header("Authorization", "Bearer " + getCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /claims/{id}/status - customer cannot reject claims (403)")
    void updateStatus_customerRejectForbidden() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-003b").claimType("ACCIDENT")
                .incidentDate(LocalDate.now()).description("Test")
                .amount(new BigDecimal("500")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        String body = "{\"status\": \"REJECTED\", \"reviewNotes\": \"Rejected\"}";

        mockMvc.perform(put("/claims/{id}/status", saved.getId())
                        .header("Authorization", "Bearer " + getCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /claims/{id}/status - adjuster can approve claim")
    void updateStatus_adjusterApprove() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-004").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test")
                .amount(new BigDecimal("2000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        String body = "{\"status\": \"APPROVED\", \"reviewNotes\": \"Looks good, approved.\"}";

        mockMvc.perform(put("/claims/{id}/status", saved.getId())
                        .header("Authorization", "Bearer " + getAdjusterToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reviewNotes").value("Looks good, approved."));
    }

    @Test
    @DisplayName("PUT /claims/{id}/status - adjuster can reject claim")
    void updateStatus_adjusterReject() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-005").claimType("FIRE")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test")
                .amount(new BigDecimal("5000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        String body = "{\"status\": \"REJECTED\", \"reviewNotes\": \"Insufficient evidence.\"}";

        mockMvc.perform(put("/claims/{id}/status", saved.getId())
                        .header("Authorization", "Bearer " + getAdjusterToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /claims/all - customer cannot access (403)")
    void getAllClaims_customerForbidden() throws Exception {
        mockMvc.perform(get("/claims/all")
                        .header("Authorization", "Bearer " + getCustomerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /claims/all - adjuster can access all claims")
    void getAllClaims_adjusterSuccess() throws Exception {
        Claim claim1 = Claim.builder()
                .claimNumber("CLM-TEST-006").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test1")
                .amount(new BigDecimal("1000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        claimRepository.save(claim1);

        mockMvc.perform(get("/claims/all")
                        .header("Authorization", "Bearer " + getAdjusterToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }
}
