package com.insurance.claimapi.dto;

import com.insurance.claimapi.entity.ClaimStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimSearchCriteria {
    private ClaimStatus status;
    private String claimNumber;
    private LocalDate fromDate;
    private LocalDate toDate;
}
