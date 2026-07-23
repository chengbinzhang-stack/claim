package com.insurance.claimapi.service.impl;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.*;
import com.insurance.claimapi.exception.*;
import com.insurance.claimapi.mapper.ClaimMapper;
import com.insurance.claimapi.repository.*;
import com.insurance.claimapi.service.ClaimService;
import com.insurance.claimapi.service.IdempotencyService;
import com.insurance.claimapi.service.NotificationServiceClient;
import com.insurance.claimapi.service.PolicyServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimMapper claimMapper;
    private final PolicyServiceClient policyServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final IdempotencyService idempotencyService;

    @Override
    @Transactional
    public ClaimDto submitClaim(ClaimRequest request, Long userId) {
        // Idempotency check
        if (request.getRequestId() != null) {
            Object cached = idempotencyService.getIfPresent(request.getRequestId());
            if (cached != null) {
                log.info("Duplicate request detected, returning cached result for requestId: {}", request.getRequestId());
                return (ClaimDto) cached;
            }
        }

        log.info("Submitting new claim for user: {}, policy: {}", userId, request.getPolicyNumber());

        // Validate policy via Policy Service
        PolicyDto policy = policyServiceClient.getPolicy(request.getPolicyNumber());

        if (policy == null) {
            throw new PolicyNotFoundException("Policy not found: " + request.getPolicyNumber());
        }

        if (!policy.isActive()) {
            throw new InvalidPolicyException("Policy is not active: " + request.getPolicyNumber());
        }

        if (policy.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidPolicyException("Policy has expired: " + request.getPolicyNumber());
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Create claim
        Claim claim = Claim.builder()
                .claimNumber(generateClaimNumber())
                .claimType(request.getClaimType())
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription())
                .amount(request.getAmount())
                .status(ClaimStatus.SUBMITTED)
                .build();

        // Set relations
        Policy dbPolicy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found: " + request.getPolicyNumber()));
        claim.setPolicy(dbPolicy);
        claim.setSubmittedBy(user);

        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim submitted successfully: {}", savedClaim.getClaimNumber());

        ClaimDto result = claimMapper.toDto(savedClaim);

        // Cache result for idempotency
        if (request.getRequestId() != null) {
            idempotencyService.put(request.getRequestId(), result);
        }

        // Send notification
        try {
            sendClaimNotification(user, savedClaim, "CLAIM_SUBMITTED");
        } catch (Exception e) {
            log.error("Failed to send notification for claim: {}", savedClaim.getClaimNumber(), e);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimDto getClaimById(Long id) {
        Claim claim = claimRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + id));
        return claimMapper.toDto(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimDto getClaimByClaimNumber(String claimNumber) {
        Claim claim = claimRepository.findByClaimNumberWithDetails(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimNumber));
        return claimMapper.toDto(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClaimDto> getMyClaims(Long userId, Pageable pageable) {
        log.debug("Fetching claims for user: {}", userId);
        return claimRepository.findBySubmittedById(userId, pageable)
                .map(claimMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClaimDto> getAllClaims(ClaimSearchCriteria criteria, Pageable pageable) {
        log.debug("Fetching all claims with criteria: {}", criteria);
        return claimRepository.findByFilters(
                criteria.getStatus(),
                criteria.getClaimNumber(),
                criteria.getFromDate(),
                criteria.getToDate(),
                pageable
        ).map(claimMapper::toDto);
    }

    @Override
    @Transactional
    public ClaimDto updateClaimStatus(Long id, ClaimUpdateStatusRequest request, Long reviewerId) {
        log.info("Updating claim {} status to {} by reviewer {}", id, request.getStatus(), reviewerId);

        Claim claim = claimRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + id));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found: " + reviewerId));

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(request.getStatus());
        claim.setReviewedBy(reviewer);
        claim.setReviewNotes(request.getReviewNotes());

        Claim updatedClaim = claimRepository.save(claim);
        log.info("Claim {} status updated from {} to {}", id, oldStatus, request.getStatus());

        // Send notification
        try {
            sendClaimNotification(claim.getSubmittedBy(), updatedClaim, "CLAIM_" + request.getStatus().name());
        } catch (Exception e) {
            log.error("Failed to send notification for claim: {}", updatedClaim.getClaimNumber(), e);
        }

        return claimMapper.toDto(updatedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        log.debug("Fetching dashboard statistics");

        List<Object[]> stats = claimRepository.countByStatusGrouped();

        DashboardStats.DashboardStatsBuilder builder = DashboardStats.builder();
        long total = 0;

        for (Object[] row : stats) {
            ClaimStatus status = (ClaimStatus) row[0];
            long count = ((Number) row[1]).longValue();
            total += count;

            switch (status) {
                case SUBMITTED -> builder.submitted(count);
                case IN_REVIEW -> builder.inReview(count);
                case APPROVED -> builder.approved(count);
                case REJECTED -> builder.rejected(count);
                case PAID -> builder.paid(count);
            }
        }

        return builder.totalClaims(total).build();
    }

    private String generateClaimNumber() {
        return "CLM-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void sendClaimNotification(User user, Claim claim, String notificationType) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject("Claim " + claim.getClaimNumber() + " - Status Update")
                .body("Your claim " + claim.getClaimNumber() + " has been updated to status: " + claim.getStatus())
                .claimNumber(claim.getClaimNumber())
                .notificationType(notificationType)
                .build();

        notificationServiceClient.sendEmail(emailRequest);
    }
}
