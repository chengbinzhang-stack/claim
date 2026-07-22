package com.insurance.claimapi.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDto {
    private String policyNumber;
    private String customerName;
    private Long customerId;
    private String policyType;
    private String policyStatus;
    private BigDecimal coverage;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private boolean active;
}
