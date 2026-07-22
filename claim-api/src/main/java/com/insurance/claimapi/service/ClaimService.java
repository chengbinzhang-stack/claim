package com.insurance.claimapi.service;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClaimService {
    ClaimDto submitClaim(ClaimRequest request, Long userId);
    ClaimDto getClaimById(Long id);
    ClaimDto getClaimByClaimNumber(String claimNumber);
    Page<ClaimDto> getMyClaims(Long userId, Pageable pageable);
    Page<ClaimDto> getAllClaims(ClaimSearchCriteria criteria, Pageable pageable);
    ClaimDto updateClaimStatus(Long id, ClaimUpdateStatusRequest request, Long reviewerId);
    DashboardStats getDashboardStats();
}
