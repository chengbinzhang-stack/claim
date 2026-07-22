package com.insurance.claimapi.dto;

import com.insurance.claimapi.entity.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimUpdateStatusRequest {

    @NotNull(message = "Status is required")
    private ClaimStatus status;

    private String reviewNotes;
}
