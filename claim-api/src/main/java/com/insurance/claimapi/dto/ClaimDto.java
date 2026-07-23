package com.insurance.claimapi.dto;

import com.insurance.claimapi.entity.ClaimStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDto {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private String policyType;
    private String claimType;
    private LocalDate incidentDate;
    private String description;
    private BigDecimal amount;
    private ClaimStatus status;
    private String submittedByName;
    private String submittedByEmail;
    private String reviewedByName;
    private String reviewNotes;
    private List<AttachmentDto> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
