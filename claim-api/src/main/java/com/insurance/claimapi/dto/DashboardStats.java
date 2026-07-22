package com.insurance.claimapi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private long totalClaims;
    private long submitted;
    private long inReview;
    private long approved;
    private long rejected;
    private long paid;
}
