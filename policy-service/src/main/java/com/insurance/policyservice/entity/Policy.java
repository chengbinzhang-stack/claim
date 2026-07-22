package com.insurance.policyservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Policy {
    @Id
    @Column(name = "policy_number", length = 50)
    private String policyNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "policy_type", nullable = false, length = 50)
    private String policyType;

    @Column(name = "policy_status", nullable = false, length = 20)
    private String policyStatus;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premium;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    public boolean isActive() {
        return "ACTIVE".equals(policyStatus) && expiryDate.isAfter(LocalDate.now());
    }
}
