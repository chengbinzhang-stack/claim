package com.insurance.claimapi.service;

import com.insurance.claimapi.dto.EmailRequest;

public interface NotificationServiceClient {
    void sendEmail(EmailRequest request);
}
