package com.insurance.claimapi.service;

import com.insurance.claimapi.dto.PolicyDto;

public interface PolicyServiceClient {
    PolicyDto getPolicy(String policyNumber);
}
