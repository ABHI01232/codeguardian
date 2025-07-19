package com.codeguardian.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "commit-analysis", groupId = "notification-group")
    public void handleCommitAnalysis(String message) {
        logger.info("Received commit analysis: {}", message);
        
        try {
            Map<String, Object> analysisData = objectMapper.readValue(message, Map.class);
            
            Map<String, Object> notification = createNotification(
                "webhook_received",
                "New commit analysis started",
                String.format("Webhook received for repository: %s", 
                    analysisData.getOrDefault("repository", "Unknown")),
                "info",
                analysisData.getOrDefault("repository", "Unknown").toString()
            );
            
            sendNotification(notification);
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing commit analysis message", e);
            
            // Send generic notification for unparseable messages
            Map<String, Object> notification = createNotification(
                "webhook_received",
                "New commit analysis started",
                "Webhook received for code analysis",
                "info",
                null
            );
            
            sendNotification(notification);
        }
    }

    @KafkaListener(topics = "analysis-progress", groupId = "notification-group")
    public void handleAnalysisProgress(String message) {
        logger.info("Received analysis progress: {}", message);
        
        try {
            Map<String, Object> progressData = objectMapper.readValue(message, Map.class);
            
            // Create progress notification for WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ANALYSIS_PROGRESS");
            notification.put("data", progressData);
            
            // Send to WebSocket topic for real-time updates
            messagingTemplate.convertAndSend("/topic/analysis-updates", notification);
            logger.info("Sent progress update: {} ({}%)", 
                progressData.get("step"), progressData.get("progress"));
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing analysis progress message", e);
        }
    }

    @KafkaListener(topics = "analysis-results", groupId = "notification-group")
    public void handleAnalysisResults(String message) {
        logger.info("Received analysis results: {}", message);
        
        try {
            Map<String, Object> resultData = objectMapper.readValue(message, Map.class);
            
            // Extract vulnerability counts
            Map<String, Object> vulnerabilities = (Map<String, Object>) resultData.get("vulnerabilities");
            Map<String, Object> findings = (Map<String, Object>) resultData.get("findings");
            int totalIssues = 0;
            String severity = "info";
            
            if (findings != null) {
                totalIssues = findings.values().stream()
                    .mapToInt(v -> v instanceof Number ? ((Number) v).intValue() : 0)
                    .sum();
                
                // Determine severity based on issue count
                if (totalIssues > 10) {
                    severity = "high";
                } else if (totalIssues > 5) {
                    severity = "medium";
                } else if (totalIssues > 0) {
                    severity = "low";
                } else {
                    severity = "success";
                }
            }
            
            // Send completion notification for WebSocket
            Map<String, Object> completionNotification = new HashMap<>();
            completionNotification.put("type", "ANALYSIS_COMPLETE");
            completionNotification.put("data", resultData);
            
            messagingTemplate.convertAndSend("/topic/analysis-updates", completionNotification);
            
            // Send regular notification
            Map<String, Object> notification = createNotification(
                "analysis_complete",
                "Security analysis completed",
                String.format("Analysis completed for %s - %d issues found", 
                    resultData.getOrDefault("repositoryName", "Unknown"), totalIssues),
                severity,
                resultData.getOrDefault("repositoryName", "Unknown").toString()
            );
            
            sendNotification(notification);
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing analysis results message", e);
            
            // Send generic notification for unparseable messages
            Map<String, Object> notification = createNotification(
                "analysis_complete",
                "Security analysis completed",
                "Code analysis has finished processing",
                "success",
                null
            );
            
            sendNotification(notification);
        }
    }

    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void handleGeneralNotifications(String message) {
        logger.info("Received general notification: {}", message);
        
        try {
            Map<String, Object> notificationData = objectMapper.readValue(message, Map.class);
            sendNotification(notificationData);
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing general notification message", e);
        }
    }

    private Map<String, Object> createNotification(String type, String title, String message, 
                                                  String severity, String repository) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", System.currentTimeMillis());
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        notification.put("severity", severity);
        notification.put("repository", repository);
        
        return notification;
    }

    private void sendNotification(Map<String, Object> notification) {
        try {
            // Send to all connected clients
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            logger.info("Sent notification: {}", notification.get("title"));
            
        } catch (Exception e) {
            logger.error("Error sending notification", e);
        }
    }

    public void sendSystemHealthNotification(String message) {
        Map<String, Object> notification = createNotification(
            "system_health",
            "System Health Check",
            message,
            "info",
            null
        );
        
        sendNotification(notification);
    }

    public void sendSecurityAlert(String message, String repository) {
        Map<String, Object> notification = createNotification(
            "security_alert",
            "Security Alert",
            message,
            "high",
            repository
        );
        
        sendNotification(notification);
    }
}