package com.insurance.claimapi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequest {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotBlank(message = "Claim type is required")
    private String claimType;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date must be in the past or present")
    private LocalDate incidentDate;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Amount exceeds maximum limit")
    private BigDecimal amount;

    private List<AttachmentDto> attachments;
}
