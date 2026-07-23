package com.insurance.claimapi.service.impl;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.*;
import com.insurance.claimapi.exception.*;
import com.insurance.claimapi.mapper.ClaimMapper;
import com.insurance.claimapi.repository.*;
import com.insurance.claimapi.service.NotificationServiceClient;
import com.insurance.claimapi.service.PolicyServiceClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private UserRepository userRepository;
    @Mock private PolicyRepository policyRepository;
    @Mock private ClaimMapper claimMapper;
    @Mock private PolicyServiceClient policyServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private User testUser;
    private Policy testPolicy;
    private Claim testClaim;
    private ClaimDto testClaimDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .fullName("Test User").enabled(true)
                .build();

        testPolicy = Policy.builder()
                .policyNumber("POL-001")
                .policyType("AUTO")
                .policyStatus("ACTIVE")
                .customer(testUser)
                .customerName("Test User")
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        testClaim = Claim.builder()
                .id(1L).claimNumber("CLM-2026-ABCD1234").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1)).description("Test claim")
                .amount(new BigDecimal("5000")).status(ClaimStatus.SUBMITTED)
                .policy(testPolicy).submittedBy(testUser)
                .build();

        testClaimDto = ClaimDto.builder()
                .id(1L).claimNumber("CLM-2026-ABCD1234").policyNumber("POL-001")
                .claimType("ACCIDENT").amount(new BigDecimal("5000"))
                .status(ClaimStatus.SUBMITTED)
                .submittedByName("Test User").submittedByEmail("test@example.com")
                .build();
    }

    @Test
    @DisplayName("submitClaim - success")
    void submitClaim_success() {
        ClaimRequest request = ClaimRequest.builder()
                .policyNumber("POL-001").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1))
                .description("Car accident on highway").amount(new BigDecimal("5000"))
                .build();

        PolicyDto policyDto = PolicyDto.builder()
                .policyNumber("POL-001").active(true)
                .expiryDate(LocalDate.now().plusYears(1)).build();

        when(policyServiceClient.getPolicy("POL-001")).thenReturn(policyDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(policyRepository.findByPolicyNumber("POL-001")).thenReturn(Optional.of(testPolicy));
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);
        when(claimMapper.toDto(any(Claim.class))).thenReturn(testClaimDto);

        ClaimDto result = claimService.submitClaim(request, 1L);

        assertNotNull(result);
        assertEquals("CLM-2026-ABCD1234", result.getClaimNumber());
        verify(claimRepository).save(any(Claim.class));
        verify(notificationServiceClient, never()).sendEmail(any());
    }

    @Test
    @DisplayName("submitClaim - policy not found throws exception")
    void submitClaim_policyNotFound() {
        ClaimRequest request = ClaimRequest.builder()
                .policyNumber("INVALID").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1))
                .description("Test").amount(new BigDecimal("1000"))
                .build();

        when(policyServiceClient.getPolicy("INVALID")).thenReturn(null);

        assertThrows(PolicyNotFoundException.class,
                () -> claimService.submitClaim(request, 1L));
    }

    @Test
    @DisplayName("submitClaim - inactive policy throws exception")
    void submitClaim_inactivePolicy() {
        ClaimRequest request = ClaimRequest.builder()
                .policyNumber("POL-001").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1))
                .description("Test").amount(new BigDecimal("1000"))
                .build();

        PolicyDto policyDto = PolicyDto.builder()
                .policyNumber("POL-001").active(false)
                .expiryDate(LocalDate.now().plusYears(1)).build();

        when(policyServiceClient.getPolicy("POL-001")).thenReturn(policyDto);

        assertThrows(InvalidPolicyException.class,
                () -> claimService.submitClaim(request, 1L));
    }

    @Test
    @DisplayName("submitClaim - expired policy throws exception")
    void submitClaim_expiredPolicy() {
        ClaimRequest request = ClaimRequest.builder()
                .policyNumber("POL-001").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1))
                .description("Test").amount(new BigDecimal("1000"))
                .build();

        PolicyDto policyDto = PolicyDto.builder()
                .policyNumber("POL-001").active(true)
                .expiryDate(LocalDate.now().minusDays(1)).build();

        when(policyServiceClient.getPolicy("POL-001")).thenReturn(policyDto);

        assertThrows(InvalidPolicyException.class,
                () -> claimService.submitClaim(request, 1L));
    }

    @Test
    @DisplayName("submitClaim - user not found throws exception")
    void submitClaim_userNotFound() {
        ClaimRequest request = ClaimRequest.builder()
                .policyNumber("POL-001").claimType("ACCIDENT")
                .incidentDate(LocalDate.now().minusDays(1))
                .description("Test").amount(new BigDecimal("1000"))
                .build();

        PolicyDto policyDto = PolicyDto.builder()
                .policyNumber("POL-001").active(true)
                .expiryDate(LocalDate.now().plusYears(1)).build();

        when(policyServiceClient.getPolicy("POL-001")).thenReturn(policyDto);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> claimService.submitClaim(request, 99L));
    }

    @Test
    @DisplayName("getClaimById - success")
    void getClaimById_success() {
        when(claimRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testClaim));
        when(claimMapper.toDto(testClaim)).thenReturn(testClaimDto);

        ClaimDto result = claimService.getClaimById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getClaimById - not found throws exception")
    void getClaimById_notFound() {
        when(claimRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> claimService.getClaimById(99L));
    }

    @Test
    @DisplayName("updateClaimStatus - approve success")
    void updateClaimStatus_approve_success() {
        ClaimUpdateStatusRequest request = ClaimUpdateStatusRequest.builder()
                .status(ClaimStatus.APPROVED).reviewNotes("Looks good")
                .build();

        User reviewer = User.builder().id(2L).username("adjuster").build();

        Claim approvedClaim = Claim.builder()
                .id(1L).claimNumber("CLM-2026-ABCD1234").status(ClaimStatus.APPROVED)
                .policy(testPolicy).submittedBy(testUser).reviewedBy(reviewer)
                .reviewNotes("Looks good").build();

        ClaimDto approvedDto = ClaimDto.builder()
                .id(1L).claimNumber("CLM-2026-ABCD1234").status(ClaimStatus.APPROVED)
                .reviewNotes("Looks good").build();

        when(claimRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testClaim));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(claimRepository.save(any(Claim.class))).thenReturn(approvedClaim);
        when(claimMapper.toDto(any(Claim.class))).thenReturn(approvedDto);

        ClaimDto result = claimService.updateClaimStatus(1L, request, 2L);

        assertEquals(ClaimStatus.APPROVED, result.getStatus());
        assertEquals("Looks good", result.getReviewNotes());
    }

    @Test
    @DisplayName("updateClaimStatus - claim not found throws exception")
    void updateClaimStatus_claimNotFound() {
        ClaimUpdateStatusRequest request = ClaimUpdateStatusRequest.builder()
                .status(ClaimStatus.APPROVED).build();

        when(claimRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> claimService.updateClaimStatus(99L, request, 1L));
    }

    @Test
    @DisplayName("updateClaimStatus - reviewer not found throws exception")
    void updateClaimStatus_reviewerNotFound() {
        ClaimUpdateStatusRequest request = ClaimUpdateStatusRequest.builder()
                .status(ClaimStatus.APPROVED).build();

        when(claimRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testClaim));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> claimService.updateClaimStatus(1L, request, 99L));
    }
}
