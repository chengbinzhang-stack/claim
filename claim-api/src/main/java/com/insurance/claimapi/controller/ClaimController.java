package com.insurance.claimapi.controller;

import java.util.List;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.User;
import com.insurance.claimapi.repository.UserRepository;
import com.insurance.claimapi.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claim management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ClaimController {

    private final ClaimService claimService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Submit a new claim", description = "Submit a new insurance claim")
    public ResponseEntity<ApiResponse<ClaimDto>> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal User user) {

        ClaimDto claim = claimService.submitClaim(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(claim, "Claim submitted successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my claims", description = "Get all claims for the current user")
    public ResponseEntity<ApiResponse<List<ClaimDto>>> getMyClaims(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ClaimDto> claims = claimService.getMyClaims(user.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(
                claims.getContent(),
                "Claims retrieved successfully",
                claims.getNumber(),
                claims.getTotalPages(),
                claims.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID", description = "Get claim details by ID")
    public ResponseEntity<ApiResponse<ClaimDto>> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ClaimDto claim = claimService.getClaimById(id);
        return ResponseEntity.ok(ApiResponse.success(claim, "Claim retrieved successfully"));
    }

    @GetMapping("/number/{claimNumber}")
    @Operation(summary = "Get claim by claim number", description = "Get claim details by claim number")
    public ResponseEntity<ApiResponse<ClaimDto>> getClaimByClaimNumber(
            @PathVariable String claimNumber) {

        ClaimDto claim = claimService.getClaimByClaimNumber(claimNumber);
        return ResponseEntity.ok(ApiResponse.success(claim, "Claim retrieved successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADJUSTER', 'ADMIN')")
    @Operation(summary = "Get all claims", description = "Get all claims with optional filters (Admin/Adjuster only)")
    public ResponseEntity<ApiResponse<List<ClaimDto>>> getAllClaims(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String claimNumber,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        ClaimSearchCriteria criteria = ClaimSearchCriteria.builder()
                .claimNumber(claimNumber)
                .build();

        Page<ClaimDto> claims = claimService.getAllClaims(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                claims.getContent(),
                "Claims retrieved successfully",
                claims.getNumber(),
                claims.getTotalPages(),
                claims.getTotalElements()
        ));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADJUSTER', 'ADMIN')")
    @Operation(summary = "Update claim status", description = "Update claim status (Adjuster/Admin only)")
    public ResponseEntity<ApiResponse<ClaimDto>> updateClaimStatus(
            @PathVariable Long id,
            @Valid @RequestBody ClaimUpdateStatusRequest request,
            @AuthenticationPrincipal User user) {

        ClaimDto claim = claimService.updateClaimStatus(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(claim, "Claim status updated successfully"));
    }
}
