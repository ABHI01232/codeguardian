package com.codeguardian.service;

import com.codeguardian.entity.AnalysisEntity;
import com.codeguardian.repository.AnalysisEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisConsumerService {

    @Autowired
    private SecurityAnalysisService securityAnalysisService;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private KafkaMonitoringService kafkaMonitoringService;
    
    @Autowired
    private AnalysisEntityRepository analysisEntityRepository;
    
    @Autowired
    private NotificationService notificationService;

    @KafkaListener(
            topics = "analysis-requests", 
            groupId = "codeguardian-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processAnalysisRequest(Map<String, Object> analysisRequest) {
        long startTime = System.currentTimeMillis();
        try {
            String analysisId = (String) analysisRequest.get("analysisId");
            String repositoryName = (String) analysisRequest.get("repositoryName");
            String repositoryUrl = (String) analysisRequest.get("repositoryUrl");
            Long repositoryId = ((Number) analysisRequest.get("repositoryId")).longValue();
            
            kafkaMonitoringService.recordMessageReceived("analysis-requests", 0);
            
            System.out.println("🔍 Processing analysis request from Kafka:");
            System.out.println("   • Analysis ID: " + analysisId);
            System.out.println("   • Repository: " + repositoryName);
            
            // Perform the actual security analysis
            Map<String, Object> analysisResult = securityAnalysisService.performSecurityAnalysis(
                repositoryName, repositoryUrl
            );
            
            // Add missing fields to analysis result
            analysisResult.put("analysisId", analysisId);
            analysisResult.put("repositoryId", repositoryId);
            
            // Send results back via Kafka
            try {
                kafkaTemplate.send("analysis-results", analysisId, analysisResult);
                kafkaMonitoringService.recordMessageSent("analysis-results");
                System.out.println("📊 Analysis results sent to Kafka topic: analysis-results");
            } catch (Exception kafkaError) {
                System.err.println("❌ Error sending to Kafka: " + kafkaError.getMessage());
                kafkaMonitoringService.recordError("analysis-results", kafkaError.getMessage());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            kafkaMonitoringService.recordMessageReceived("analysis-requests", processingTime);
            
        } catch (Exception e) {
            kafkaMonitoringService.recordError("analysis-requests", e.getMessage());
            System.err.println("❌ Error processing analysis request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(
            topics = "analysis-results", 
            groupId = "codeguardian-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAnalysisResults(Map<String, Object> analysisResult) {
        try {
            String analysisId = (String) analysisResult.get("analysisId");
            String repositoryName = (String) analysisResult.get("repositoryName");
            Long repositoryId = ((Number) analysisResult.get("repositoryId")).longValue();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) analysisResult.get("summary");
            
            System.out.println("📊 Received analysis results from Kafka:");
            System.out.println("   • Analysis ID: " + analysisId);
            System.out.println("   • Repository: " + repositoryName);
            System.out.println("   • Total Findings: " + summary.get("totalFindings"));
            System.out.println("   • Risk Score: " + summary.get("riskScore"));
            
            // Create and save analysis entity to database
            AnalysisEntity analysis = new AnalysisEntity();
            analysis.setId(analysisId);
            analysis.setRepositoryId(repositoryId);
            analysis.setRepositoryName(repositoryName);
            analysis.setStatus("COMPLETED");
            analysis.setTimestamp(LocalDateTime.now());
            analysis.setCreatedAt(LocalDateTime.now());
            analysis.setUpdatedAt(LocalDateTime.now());
            
            // Set findings from summary
            if (summary != null) {
                analysis.setCriticalFindings(getIntegerValue(summary, "criticalCount"));
                analysis.setHighFindings(getIntegerValue(summary, "highCount"));
                analysis.setMediumFindings(getIntegerValue(summary, "mediumCount"));
                analysis.setLowFindings(getIntegerValue(summary, "lowCount"));
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> findings = (List<Map<String, Object>>) analysisResult.get("findings");
                if (findings != null) {
                    analysis.setFindingsDetails(findings.toString());
                }
            }
            
            // Save to database
            analysisEntityRepository.save(analysis);
            System.out.println("💾 Analysis results saved to database: " + analysisId);
            
            // Send WebSocket notifications to frontend
            notificationService.sendAnalysisComplete(analysisId, repositoryName, summary);
            
            // Send security alerts for high severity findings
            if (summary != null) {
                Integer criticalCount = getIntegerValue(summary, "criticalCount");
                Integer highCount = getIntegerValue(summary, "highCount");
                
                if (criticalCount > 0) {
                    notificationService.sendSecurityAlert(analysisId, repositoryName, "CRITICAL", "vulnerabilities");
                }
                if (highCount > 0) {
                    notificationService.sendSecurityAlert(analysisId, repositoryName, "HIGH", "vulnerabilities");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error handling analysis results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
}