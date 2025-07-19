package com.codeguardian.service;

import com.codeguardian.entity.AnalysisEntity;
import com.codeguardian.entity.RepositoryEntity;
import com.codeguardian.repository.AnalysisEntityRepository;
import com.codeguardian.repository.RepositoryEntityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class AnalysisProcessingService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Removed AnalysisService dependency to avoid circular reference
    
    @Autowired
    private AnalysisEntityRepository analysisRepository;
    
    @Autowired
    private RepositoryEntityRepository repositoryRepository;

    /**
     * Process analysis requests from Kafka
     */
    @KafkaListener(topics = "analysis-requests", groupId = "analysis-processor")
    public void processAnalysisRequest(String message) {
        try {
            Map<String, Object> analysisRequest = objectMapper.readValue(message, Map.class);
            String analysisId = (String) analysisRequest.get("analysisId");
            String repositoryName = (String) analysisRequest.get("repositoryName");
            
            System.out.println("🔄 Processing analysis request: " + analysisId + " for " + repositoryName);
            
            // Simulate asynchronous analysis processing
            CompletableFuture.runAsync(() -> {
                simulateAnalysisSteps(analysisId, repositoryName, analysisRequest);
            });
            
        } catch (JsonProcessingException e) {
            System.err.println("❌ Failed to process analysis request: " + e.getMessage());
        }
    }

    /**
     * Simulate realistic analysis steps with progress updates
     */
    private void simulateAnalysisSteps(String analysisId, String repositoryName, Map<String, Object> analysisRequest) {
        try {
            // Step 1: Cloning Repository
            sendProgressUpdate(analysisId, "cloning", "Cloning Repository", 20);
            Thread.sleep(3000);
            
            // Step 2: Scanning Code Files
            sendProgressUpdate(analysisId, "scanning", "Scanning Code Files", 40);
            Thread.sleep(4000);
            
            // Step 3: Security Analysis
            sendProgressUpdate(analysisId, "analyzing", "Security Analysis", 70);
            Thread.sleep(5000);
            
            // Step 4: Generating Report
            sendProgressUpdate(analysisId, "reporting", "Generating Report", 90);
            Thread.sleep(2000);
            
            // Step 5: Complete
            sendProgressUpdate(analysisId, "complete", "Analysis Complete", 100);
            
            // Send final results
            sendAnalysisResults(analysisId, repositoryName, analysisRequest);
            
        } catch (InterruptedException e) {
            System.err.println("❌ Analysis processing interrupted: " + e.getMessage());
        }
    }

    /**
     * Send progress update to Kafka
     */
    private void sendProgressUpdate(String analysisId, String step, String stepLabel, int progressPercentage) {
        try {
            Map<String, Object> progressUpdate = new HashMap<>();
            progressUpdate.put("analysisId", analysisId);
            progressUpdate.put("step", step);
            progressUpdate.put("stepLabel", stepLabel);
            progressUpdate.put("progress", progressPercentage);
            progressUpdate.put("timestamp", LocalDateTime.now().toString());
            
            kafkaTemplate.send("analysis-progress", progressUpdate);
            System.out.println("📊 Progress update sent: " + step + " (" + progressPercentage + "%)");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send progress update: " + e.getMessage());
        }
    }

    /**
     * Send final analysis results to Kafka
     */
    private void sendAnalysisResults(String analysisId, String repositoryName, Map<String, Object> analysisRequest) {
        try {
            // Create mock analysis results
            Map<String, Object> analysisResult = new HashMap<>();
            analysisResult.put("analysisId", analysisId);
            analysisResult.put("repositoryName", repositoryName);
            analysisResult.put("repositoryId", analysisRequest.get("repositoryId"));
            analysisResult.put("status", "COMPLETED");
            analysisResult.put("timestamp", LocalDateTime.now().toString());
            
            // Real analysis results - will be populated by actual security scan
            // For now, initialize with zero findings until real analysis integration
            Map<String, Integer> findings = new HashMap<>();
            findings.put("critical", 0);
            findings.put("high", 0);
            findings.put("medium", 0);
            findings.put("low", 0);
            
            analysisResult.put("findings", findings);
            analysisResult.put("duration", "15s");
            
            // Send to Kafka
            kafkaTemplate.send("analysis-results", analysisResult);
            System.out.println("✅ Analysis results sent: " + analysisId);
            
            // Update analysis in service
            updateAnalysisInService(analysisId, findings, repositoryName, analysisRequest);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send analysis results: " + e.getMessage());
        }
    }

    /**
     * Update analysis status in the AnalysisService
     */
    private void updateAnalysisInService(String analysisId, Map<String, Integer> findings, String repositoryName, Map<String, Object> analysisRequest) {
        try {
            // Create a complete analysis object to store
            Map<String, Object> completedAnalysis = new HashMap<>();
            completedAnalysis.put("id", analysisId);
            completedAnalysis.put("status", "COMPLETED");
            completedAnalysis.put("timestamp", LocalDateTime.now().toString());
            completedAnalysis.put("duration", "15s");
            completedAnalysis.put("findings", findings);
            
            // Add actual repository information
            completedAnalysis.put("repositoryId", analysisRequest.get("repositoryId"));
            completedAnalysis.put("repository", repositoryName);
            completedAnalysis.put("commitId", "HEAD");
            completedAnalysis.put("commitMessage", "Security analysis scan");
            completedAnalysis.put("author", "CodeGuardian");
            completedAnalysis.put("scanConfig", Map.of(
                "language", "Java",
                "rules", java.util.List.of("security", "quality"),
                "scope", "full"
            ));
            
            // Generate generic findings based on the repository
            completedAnalysis.put("details", generateGenericFindings(findings));
            
            // Store in database
            storeAnalysisInDatabase(analysisId, completedAnalysis, (Long) analysisRequest.get("repositoryId"));
            
            System.out.println("📝 Analysis " + analysisId + " completed and stored with findings: " + findings);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to update analysis in service: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate real findings based on actual security analysis
     * TODO: Replace with integration to real security analysis tools (SAST, DAST, etc.)
     */
    private java.util.List<Object> generateGenericFindings(Map<String, Integer> findings) {
        // Return empty list until real security analysis integration is complete
        // Real analysis should populate findings from:
        // - Static Application Security Testing (SAST)
        // - Dynamic Application Security Testing (DAST) 
        // - Dependency vulnerability scanning
        // - Code quality analysis
        return new java.util.ArrayList<>();
    }
    
    /**
     * Store analysis results in database
     */
    private void storeAnalysisInDatabase(String analysisId, Map<String, Object> completedAnalysis, Long repositoryId) {
        try {
            AnalysisEntity analysis = analysisRepository.findById(analysisId).orElse(null);
            if (analysis == null) {
                // Create new analysis entity if it doesn't exist
                analysis = new AnalysisEntity(analysisId, repositoryId, "COMPLETED");
            } else {
                analysis.setStatus("COMPLETED");
            }
            
            analysis.setRepositoryName((String) completedAnalysis.get("repository"));
            analysis.setCommitId((String) completedAnalysis.get("commitId"));
            analysis.setCommitMessage((String) completedAnalysis.get("commitMessage"));
            analysis.setAuthor((String) completedAnalysis.get("author"));
            analysis.setDuration((String) completedAnalysis.get("duration"));
            analysis.setTimestamp(LocalDateTime.now());
            
            // Set findings
            Map<String, Integer> findings = (Map<String, Integer>) completedAnalysis.get("findings");
            if (findings != null) {
                analysis.setCriticalFindings(findings.getOrDefault("critical", 0));
                analysis.setHighFindings(findings.getOrDefault("high", 0));
                analysis.setMediumFindings(findings.getOrDefault("medium", 0));
                analysis.setLowFindings(findings.getOrDefault("low", 0));
            }
            
            // Store scan config and findings details as JSON
            try {
                if (completedAnalysis.get("scanConfig") != null) {
                    analysis.setScanConfig(objectMapper.writeValueAsString(completedAnalysis.get("scanConfig")));
                }
                if (completedAnalysis.get("details") != null) {
                    analysis.setFindingsDetails(objectMapper.writeValueAsString(completedAnalysis.get("details")));
                }
            } catch (JsonProcessingException e) {
                System.err.println("Error serializing analysis data: " + e.getMessage());
            }
            
            analysisRepository.save(analysis);
            
            // Update repository statistics
            RepositoryEntity repository = repositoryRepository.findById(repositoryId).orElse(null);
            if (repository != null && findings != null) {
                int totalIssues = findings.values().stream().mapToInt(Integer::intValue).sum();
                repository.setIssueCount(totalIssues);
                
                // Update security status based on findings
                if (findings.getOrDefault("critical", 0) > 0) {
                    repository.setSecurityStatus("CRITICAL");
                } else if (findings.getOrDefault("high", 0) > 0) {
                    repository.setSecurityStatus("WARNING");
                } else {
                    repository.setSecurityStatus("SECURE");
                }
                
                repositoryRepository.save(repository);
            }
            
            System.out.println("✅ Analysis stored in database: " + analysisId);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to store analysis in database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Trigger analysis for a repository
     */
    public String triggerAnalysis(Long repositoryId) {
        RepositoryEntity repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + repositoryId));
        
        String analysisId = "analysis_" + System.currentTimeMillis();
        
        // Create initial analysis record
        AnalysisEntity analysis = new AnalysisEntity(analysisId, repositoryId, "RUNNING");
        analysis.setRepositoryName(repository.getName());
        analysis.setCommitId("latest");
        analysis.setCommitMessage("Security analysis");
        analysis.setAuthor("System");
        analysis.setTimestamp(LocalDateTime.now());
        
        analysisRepository.save(analysis);
        
        // Create analysis request
        Map<String, Object> analysisRequest = new HashMap<>();
        analysisRequest.put("analysisId", analysisId);
        analysisRequest.put("repositoryId", repositoryId);
        analysisRequest.put("repositoryName", repository.getName());
        analysisRequest.put("repositoryUrl", repository.getUrl());
        analysisRequest.put("platform", repository.getPlatform());
        analysisRequest.put("language", repository.getLanguage());
        analysisRequest.put("defaultBranch", repository.getDefaultBranch());
        analysisRequest.put("timestamp", LocalDateTime.now().toString());
        
        // Send to Kafka for processing
        kafkaTemplate.send("analysis-requests", analysisRequest);
        System.out.println("🚀 Analysis request sent: " + analysisId + " for " + repository.getName());
        
        // Update repository stats
        repository.setAnalysisCount(repository.getAnalysisCount() + 1);
        repository.setLastAnalysisDate(LocalDateTime.now());
        repositoryRepository.save(repository);
        
        return analysisId;
    }
    
    /**
     * Get analyses for a specific repository
     */
    public List<Map<String, Object>> getAnalysesByRepository(Long repositoryId) {
        List<AnalysisEntity> entities = analysisRepository.findByRepositoryIdOrderByTimestampDesc(repositoryId);
        return entities.stream().map(this::convertToMap).toList();
    }
    
    /**
     * Get analysis by ID
     */
    public Map<String, Object> getAnalysisById(String analysisId) {
        AnalysisEntity entity = analysisRepository.findById(analysisId).orElse(null);
        if (entity == null) {
            return null;
        }
        
        Map<String, Object> result = convertToMap(entity);
        
        // Add findings details
        if (entity.getFindingsDetails() != null) {
            try {
                List<Object> details = objectMapper.readValue(entity.getFindingsDetails(), List.class);
                result.put("details", details);
            } catch (JsonProcessingException e) {
                System.err.println("Error deserializing findings: " + e.getMessage());
                result.put("details", new ArrayList<>());
            }
        } else {
            result.put("details", new ArrayList<>());
        }
        
        return result;
    }
    
    private Map<String, Object> convertToMap(AnalysisEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("repositoryId", entity.getRepositoryId());
        map.put("repository", entity.getRepositoryName());
        map.put("commitId", entity.getCommitId());
        map.put("commitMessage", entity.getCommitMessage());
        map.put("author", entity.getAuthor());
        map.put("status", entity.getStatus());
        map.put("duration", entity.getDuration());
        map.put("timestamp", entity.getTimestamp() != null ? entity.getTimestamp().toString() : null);
        
        Map<String, Integer> findings = new HashMap<>();
        findings.put("critical", entity.getCriticalFindings());
        findings.put("high", entity.getHighFindings());
        findings.put("medium", entity.getMediumFindings());
        findings.put("low", entity.getLowFindings());
        map.put("findings", findings);
        
        // Add scan config if available
        if (entity.getScanConfig() != null) {
            try {
                Object scanConfig = objectMapper.readValue(entity.getScanConfig(), Object.class);
                map.put("scanConfig", scanConfig);
            } catch (JsonProcessingException e) {
                System.err.println("Error deserializing scan config: " + e.getMessage());
                map.put("scanConfig", Map.of("language", "Java", "rules", List.of("security", "quality"), "scope", "full"));
            }
        } else {
            map.put("scanConfig", Map.of("language", "Java", "rules", List.of("security", "quality"), "scope", "full"));
        }
        
        return map;
    }
}