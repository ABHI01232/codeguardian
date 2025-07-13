package com.codeguardian.gitprocessor.controller;

import com.codeguardian.gitprocessor.model.GitHubWebhookPayload;
import com.codeguardian.gitprocessor.model.GitLabWebhookPayload;
import com.codeguardian.gitprocessor.service.WebhookProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookProcessorService webhookProcessorService;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody GitHubWebhookPayload payload) {

        webhookProcessorService.processGitHubWebhook(eventType, payload);
        return ResponseEntity.ok("Webhook processed successfully");
    }

    @PostMapping("/gitlab")
    public ResponseEntity<String> handleGitLabWebhook(
            @RequestHeader("X-Gitlab-Event") String eventType,
            @RequestHeader("X-Gitlab-Token") String token,
            @RequestBody GitLabWebhookPayload payload) {

        webhookProcessorService.processGitLabWebhook(eventType, payload);
        return ResponseEntity.ok("Webhook processed successfully");
    }
}
