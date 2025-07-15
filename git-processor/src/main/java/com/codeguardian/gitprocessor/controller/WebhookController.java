package com.codeguardian.gitprocessor.controller;

import com.codeguardian.gitprocessor.model.GitHubWebhookPayload;
import com.codeguardian.gitprocessor.model.GitLabWebhookPayload;
import com.codeguardian.gitprocessor.security.WebhookSecurityValidator;
import com.codeguardian.gitprocessor.service.WebhookProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookProcessorService webhookProcessorService;
    private final WebhookSecurityValidator securityValidator;

    @PostMapping("/github")
    public ResponseEntity<Map<String, Object>> handleGitHubWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestBody GitHubWebhookPayload payload) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Received GitHub webhook: eventType={}, deliveryId={}", eventType, deliveryId);

            // Note: Signature validation skipped in current implementation
            // For production, implement proper signature validation with raw payload access

            // Validate payload
            if (payload == null) {
                log.warn("GitHub webhook payload is null for delivery: {}", deliveryId);
                response.put("error", "Invalid payload");
                response.put("code", "INVALID_PAYLOAD");
                return ResponseEntity.badRequest().body(response);
            }

            // Process webhook
            webhookProcessorService.processGitHubWebhook(eventType, payload);

            // Success response
            response.put("status", "success");
            response.put("message", "Webhook processed successfully");
            response.put("eventType", eventType);
            response.put("deliveryId", deliveryId);
            response.put("timestamp", System.currentTimeMillis());

            if (payload.getRepository() != null) {
                response.put("repository", payload.getRepository().getFull_name());
            }

            log.info("Successfully processed GitHub webhook: eventType={}, deliveryId={}", eventType, deliveryId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing GitHub webhook: eventType={}, deliveryId={}", eventType, deliveryId, e);

            response.put("status", "error");
            response.put("message", "Error processing webhook: " + e.getMessage());
            response.put("code", "PROCESSING_ERROR");
            response.put("eventType", eventType);
            response.put("deliveryId", deliveryId);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/gitlab")
    public ResponseEntity<Map<String, Object>> handleGitLabWebhook(
            @RequestHeader("X-Gitlab-Event") String eventType,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            @RequestHeader(value = "X-Gitlab-Event-UUID", required = false) String eventId,
            @RequestBody GitLabWebhookPayload payload) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Received GitLab webhook: eventType={}, eventId={}", eventType, eventId);

            // Validate token if provided
            if (token != null && !securityValidator.validateGitLabToken(token)) {
                log.warn("GitLab webhook token validation failed for event: {}", eventId);
                response.put("error", "Invalid token");
                response.put("code", "INVALID_TOKEN");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Validate payload
            if (payload == null) {
                log.warn("GitLab webhook payload is null for event: {}", eventId);
                response.put("error", "Invalid payload");
                response.put("code", "INVALID_PAYLOAD");
                return ResponseEntity.badRequest().body(response);
            }

            // Process webhook
            webhookProcessorService.processGitLabWebhook(eventType, payload);

            // Success response
            response.put("status", "success");
            response.put("message", "Webhook processed successfully");
            response.put("eventType", eventType);
            response.put("eventId", eventId);
            response.put("timestamp", System.currentTimeMillis());

            if (payload.getProject() != null) {
                response.put("project", payload.getProject().getName());
            }

            log.info("Successfully processed GitLab webhook: eventType={}, eventId={}", eventType, eventId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing GitLab webhook: eventType={}, eventId={}", eventType, eventId, e);

            response.put("status", "error");
            response.put("message", "Error processing webhook: " + e.getMessage());
            response.put("code", "PROCESSING_ERROR");
            response.put("eventType", eventType);
            response.put("eventId", eventId);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "git-processor");
        health.put("timestamp", System.currentTimeMillis());
        health.put("security", securityValidator.isSecurityConfigured());

        return ResponseEntity.ok(health);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getWebhookConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("securityConfigured", securityValidator.isSecurityConfigured());
        config.put("supportedPlatforms", new String[]{"GitHub", "GitLab"});
        config.put("endpoints", Map.of(
                "github", "/webhooks/github",
                "gitlab", "/webhooks/gitlab"
        ));

        return ResponseEntity.ok(config);
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook(
            @RequestParam(required = false, defaultValue = "github") String platform,
            @RequestBody(required = false) String payload) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Test webhook request for platform: {}", platform);

            response.put("status", "success");
            response.put("message", "Test webhook endpoint is working");
            response.put("platform", platform);
            response.put("timestamp", System.currentTimeMillis());
            response.put("payloadReceived", payload != null);

            if (payload != null) {
                response.put("payloadLength", payload.length());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in test webhook", e);

            response.put("status", "error");
            response.put("message", "Test webhook failed: " + e.getMessage());
            response.put("platform", platform);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Exception handler for this controller
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unhandled exception in WebhookController", e);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Internal server error");
        response.put("code", "INTERNAL_ERROR");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}