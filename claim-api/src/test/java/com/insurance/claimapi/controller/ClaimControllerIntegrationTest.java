package com.insurance.claimapi.controller;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.*;
import com.insurance.claimapi.repository.*;
import com.insurance.claimapi.service.NotificationServiceClient;
import com.insurance.claimapi.service.PolicyServiceClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ClaimControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClaimRepository claimRepository;
    @Autowired private PolicyRepository policyRepository;

    @MockBean private PolicyServiceClient policyServiceClient;
    @MockBean private NotificationServiceClient notificationServiceClient;

    private User customerUser;
    private Role customerRole;
    private Role adjusterRole;
    private Policy testPolicy;

    @BeforeEach
    void setUp() {
        customerRole = roleRepository.findByName("CUSTOMER").orElseGet(() ->
                roleRepository.save(Role.builder().name("CUSTOMER").description("Customer").build()));

        adjusterRole = roleRepository.findByName("ADJUSTER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ADJUSTER").description("Adjuster").build()));

        customerUser = userRepository.save(User.builder()
                .username("customer1").password("encoded").email("customer@test.com")
                .fullName("Customer One").role(customerRole).enabled(true).build());

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

    @Test
    @DisplayName("POST /claims - unauthenticated returns 403")
    void submitClaim_unauthenticated() throws Exception {
        mockMvc.perform(post("/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("POST /claims - customer submits claim successfully")
    void submitClaim_success() throws Exception {
        PolicyDto mockPolicy = PolicyDto.builder()
                .policyNumber("POL-TEST-001").active(true)
                .expiryDate(LocalDate.now().plusYears(1)).build();
        when(policyServiceClient.getPolicy("POL-TEST-001")).thenReturn(mockPolicy);

        String requestBody = "{\"policyNumber\": \"POL-TEST-001\", \"claimType\": \"ACCIDENT\", \"incidentDate\": \"" + LocalDate.now().minusDays(1).toString() + "\", \"description\": \"Car accident on highway ramp\", \"amount\": 5000.00}";

        mockMvc.perform(post("/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber", startsWith("CLM-")))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("POST /claims - validation errors return 400")
    void submitClaim_validationError() throws Exception {
        String requestBody = "{\"policyNumber\": \"\", \"claimType\": \"\", \"incidentDate\": null, \"description\": \"short\", \"amount\": -100}";

        mockMvc.perform(post("/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("GET /claims/my - returns only user's claims")
    void getMyClaims_success() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-001").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test")
                .amount(new BigDecimal("1000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        claimRepository.save(claim);

        mockMvc.perform(get("/claims/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-TEST-001"));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("GET /claims/{id} - returns claim details")
    void getClaimById_success() throws Exception {
        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-002").claimType("THEFT")
                .incidentDate(LocalDate.now().minusDays(2)).description("Stolen items")
                .amount(new BigDecimal("3000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        mockMvc.perform(get("/claims/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-TEST-002"));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("GET /claims/{id} - not found returns 404")
    void getClaimById_notFound() throws Exception {
        mockMvc.perform(get("/claims/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adjuster1", roles = {"ADJUSTER"})
    @DisplayName("PUT /claims/{id}/status - adjuster can approve claim")
    void updateStatus_adjusterApprove() throws Exception {
        User adjuster = userRepository.save(User.builder()
                .username("adjuster1").password("encoded").email("adjuster@test.com")
                .fullName("Adjuster One").role(adjusterRole).enabled(true).build());

        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-004").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test")
                .amount(new BigDecimal("2000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        String body = "{\"status\": \"APPROVED\", \"reviewNotes\": \"Looks good, approved.\"}";

        mockMvc.perform(put("/claims/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reviewNotes").value("Looks good, approved."));
    }

    @Test
    @WithMockUser(username = "adjuster1", roles = {"ADJUSTER"})
    @DisplayName("PUT /claims/{id}/status - adjuster can reject claim")
    void updateStatus_adjusterReject() throws Exception {
        User adjuster = userRepository.save(User.builder()
                .username("adjuster2").password("encoded").email("adjuster2@test.com")
                .fullName("Adjuster Two").role(adjusterRole).enabled(true).build());

        Claim claim = Claim.builder()
                .claimNumber("CLM-TEST-005").claimType("FIRE")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test")
                .amount(new BigDecimal("5000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        Claim saved = claimRepository.save(claim);

        String body = "{\"status\": \"REJECTED\", \"reviewNotes\": \"Insufficient evidence.\"}";

        mockMvc.perform(put("/claims/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("GET /claims/all - customer cannot access (403)")
    void getAllClaims_customerForbidden() throws Exception {
        mockMvc.perform(get("/claims/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adjuster1", roles = {"ADJUSTER"})
    @DisplayName("GET /claims/all - adjuster can access all claims")
    void getAllClaims_adjusterSuccess() throws Exception {
        User adjuster = userRepository.save(User.builder()
                .username("adjuster3").password("encoded").email("adjuster3@test.com")
                .fullName("Adjuster Three").role(adjusterRole).enabled(true).build());

        Claim claim1 = Claim.builder()
                .claimNumber("CLM-TEST-006").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test1")
                .amount(new BigDecimal("1000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(customerUser)
                .build();
        claimRepository.save(claim1);

        mockMvc.perform(get("/claims/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }
}
