package com.codeguardian.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendAnalysisComplete(String analysisId, String repositoryName, Map<String, Object> summary) {
        try {

        Map<String, Object> notification = Map.of(
            "type", "ANALYSIS_COMPLETE",
            "title", "🛡️ Analysis Complete",
            "message", "Security analysis completed for " + repositoryName,
            "analysisId", analysisId,
            "repositoryName", repositoryName,
            "summary", summary,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "priority", "high"
        );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            System.out.println("📡 WebSocket notification sent: Analysis complete for " + repositoryName);
        } catch (Exception e) {
            System.err.println("⚠️ Error sending WebSocket notification: " + e.getMessage());
        }
    }

    public void sendWebhookReceived(String repositoryName, String webhookType) {
        try {

        Map<String, Object> notification = Map.of(
            "type", "WEBHOOK_RECEIVED",
            "title", "🔗 Webhook Received",
            "message", "New commit received from " + repositoryName,
            "repositoryName", repositoryName,
            "webhookType", webhookType,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "priority", "medium"
        );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            System.out.println("📡 WebSocket notification sent: Webhook received for " + repositoryName);
        } catch (Exception e) {
            System.err.println("⚠️ Error sending WebSocket notification: " + e.getMessage());
        }
    }

    public void sendSecurityAlert(String analysisId, String repositoryName, String severity, String alertType) {
        try {

        Map<String, Object> notification = Map.of(
            "type", "SECURITY_ALERT",
            "title", "⚠️ Security Alert",
            "message", severity + " severity " + alertType + " found in " + repositoryName,
            "analysisId", analysisId,
            "repositoryName", repositoryName,
            "severity", severity,
            "alertType", alertType,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "priority", severity.equals("CRITICAL") || severity.equals("HIGH") ? "high" : "medium"
        );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            System.out.println("📡 WebSocket notification sent: Security alert for " + repositoryName);
        } catch (Exception e) {
            System.err.println("⚠️ Error sending WebSocket notification: " + e.getMessage());
        }
    }

    public void sendSystemHealthCheck(String status, String message) {
        try {

        Map<String, Object> notification = Map.of(
            "type", "SYSTEM_HEALTH",
            "title", "✅ System Health",
            "message", message,
            "status", status,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "priority", "low"
        );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            System.out.println("📡 WebSocket notification sent: System health check");
        } catch (Exception e) {
            System.err.println("⚠️ Error sending WebSocket notification: " + e.getMessage());
        }
    }

    public void sendErrorNotification(String errorType, String errorMessage, String context) {
        try {

        Map<String, Object> notification = Map.of(
            "type", "ERROR",
            "title", "❌ Error",
            "message", errorType + ": " + errorMessage,
            "context", context,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "priority", "high"
        );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            System.out.println("📡 WebSocket notification sent: Error notification");
        } catch (Exception e) {
            System.err.println("⚠️ Error sending WebSocket notification: " + e.getMessage());
        }
    }
}