package com.insurance.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Notification endpoints")
public class NotificationController {

    @PostMapping("/email")
    @Operation(summary = "Send email", description = "Send email notification (mock implementation)")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> request) {
        log.info("Sending email to: {}, subject: {}", request.get("to"), request.get("subject"));
        // Mock implementation - just log the email
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email sent successfully (mock)",
                "to", request.get("to"),
                "subject", request.get("subject")
        ));
    }
}
