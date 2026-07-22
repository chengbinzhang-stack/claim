package com.insurance.claimapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @Column(name = "policy_number", length = 50)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "policy_type", nullable = false, length = 50)
    private String policyType;

    @Column(name = "policy_status", nullable = false, length = 20)
    @Builder.Default
    private String policyStatus = "ACTIVE";

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premium;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return "ACTIVE".equals(policyStatus) && expiryDate.isAfter(LocalDate.now());
    }
}
