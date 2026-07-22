package com.insurance.claimapi.client;

import com.insurance.claimapi.dto.EmailRequest;
import com.insurance.claimapi.service.NotificationServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceClientImpl implements NotificationServiceClient {

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendEmailFallback")
    @Retry(name = "notificationService")
    public void sendEmail(EmailRequest request) {
        log.info("Sending email notification: {} to {}", request.getSubject(), request.getTo());
        // Mock implementation - in real scenario, this would call Notification Service via OpenFeign
        log.info("Email sent successfully (mock): {} - {}", request.getTo(), request.getSubject());
    }

    public void sendEmailFallback(EmailRequest request, Throwable t) {
        log.error("Notification Service fallback triggered for email: {}, error: {}", request.getTo(), t.getMessage());
        // In production, you might want to store this in a queue for retry
    }
}
