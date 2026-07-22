package com.insurance.claimapi.client;

import com.insurance.claimapi.dto.EmailRequest;
import com.insurance.claimapi.dto.PolicyDto;
import com.insurance.claimapi.service.NotificationServiceClient;
import com.insurance.claimapi.service.PolicyServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceClientImpl implements PolicyServiceClient {

    // In a real implementation, this would be injected via OpenFeign
    // For now, we return a mock implementation for demonstration
    private PolicyDto mockPolicy;

    @Override
    @CircuitBreaker(name = "policyService", fallbackMethod = "getPolicyFallback")
    @Retry(name = "policyService")
    public PolicyDto getPolicy(String policyNumber) {
        log.info("Calling Policy Service to validate policy: {}", policyNumber);
        // Mock implementation - in real scenario, this would call Policy Service via OpenFeign
        return getMockPolicy(policyNumber);
    }

    public PolicyDto getPolicyFallback(String policyNumber, Throwable t) {
        log.error("Policy Service fallback triggered for policy: {}, error: {}", policyNumber, t.getMessage());
        throw new RuntimeException("Policy Service is unavailable. Please try again later.");
    }

    private PolicyDto getMockPolicy(String policyNumber) {
        return PolicyDto.builder()
                .policyNumber(policyNumber)
                .customerName("John Doe")
                .customerId(1L)
                .policyType("HEALTH")
                .policyStatus("ACTIVE")
                .coverage(new java.math.BigDecimal("100000.00"))
                .startDate(java.time.LocalDate.now().minusYears(1))
                .expiryDate(java.time.LocalDate.now().plusYears(1))
                .active(true)
                .build();
    }
}
