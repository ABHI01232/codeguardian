package com.codeguardian.analyzer.producer;

import com.codeguardian.analyzer.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.analysis-results}")
    private String analysisResultsTopic;

    public void sendResult(AnalysisResult result) {
        try {
            log.info("Sending analysis result for commit: {} to topic: {}", 
                result.getCommitHash(), analysisResultsTopic);
            
            // Send to analysis results topic
            kafkaTemplate.send(analysisResultsTopic, result.getCommitHash(), result)
                .whenComplete((sendResult, ex) -> {
                    if (ex == null) {
                        log.info("Analysis result sent successfully for commit: {} with offset: {}", 
                            result.getCommitHash(), sendResult.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send analysis result for commit: {}", 
                            result.getCommitHash(), ex);
                    }
                });
                
            // Also send a formatted notification for WebSocket
            sendNotificationMessage(result);
            
        } catch (Exception e) {
            log.error("Error sending analysis result: {}", e.getMessage(), e);
        }
    }
    
    private void sendNotificationMessage(AnalysisResult result) {
        try {
            // Create a notification message for the WebSocket
            java.util.Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("id", System.currentTimeMillis());
            notification.put("type", "analysis_complete");
            notification.put("title", "Security Analysis Complete");
            
            // Calculate total issues
            int totalIssues = result.getSecurityIssues().size() + result.getCodeQualityIssues().size();
            String severity = determineSeverity(result.getSecurityIssues());
            
            notification.put("message", String.format("Analysis completed for commit %s - %d issues found", 
                result.getCommitHash(), totalIssues));
            notification.put("severity", severity);
            notification.put("repository", extractRepositoryName(result.getRepositoryUrl()));
            notification.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Send to notifications topic for WebSocket
            kafkaTemplate.send("notifications", notification);
            log.info("Notification sent for analysis result: {}", result.getCommitHash());
            
        } catch (Exception e) {
            log.error("Error sending notification message: {}", e.getMessage(), e);
        }
    }
    
    private String determineSeverity(java.util.List<com.codeguardian.analyzer.model.AnalysisResult.SecurityIssue> issues) {
        long criticalCount = issues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        long highCount = issues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        if (criticalCount > 0) return "high";
        if (highCount > 0) return "medium";
        if (!issues.isEmpty()) return "low";
        return "success";
    }
    
    private String extractRepositoryName(String repositoryUrl) {
        if (repositoryUrl == null) return "unknown";
        String[] parts = repositoryUrl.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "unknown";
    }
}