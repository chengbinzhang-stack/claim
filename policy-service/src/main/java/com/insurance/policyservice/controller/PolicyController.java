package com.insurance.policyservice.controller;

import com.insurance.policyservice.dto.PolicyDto;
import com.insurance.policyservice.entity.Policy;
import com.insurance.policyservice.repository.PolicyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
@Tag(name = "Policy", description = "Policy management endpoints")
public class PolicyController {

    private final PolicyRepository policyRepository;

    @GetMapping("/{policyNumber}")
    @Operation(summary = "Get policy by number", description = "Get policy details by policy number")
    public ResponseEntity<?> getPolicy(@PathVariable String policyNumber) {
        return policyRepository.findById(policyNumber)
                .map(policy -> {
                    PolicyDto dto = PolicyDto.builder()
                            .policyNumber(policy.getPolicyNumber())
                            .customerName(policy.getCustomerName())
                            .customerId(policy.getCustomerId())
                            .policyType(policy.getPolicyType())
                            .policyStatus(policy.getPolicyStatus())
                            .coverage(policy.getCoverage())
                            .startDate(policy.getStartDate())
                            .expiryDate(policy.getExpiryDate())
                            .active(policy.isActive())
                            .build();
                    return ResponseEntity.ok(Map.of("success", true, "data", dto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all policies", description = "Get all policies")
    public ResponseEntity<?> getAllPolicies() {
        return ResponseEntity.ok(Map.of("success", true, "data", policyRepository.findAll()));
    }
}
