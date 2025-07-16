package com.codeguardian.analyzer.consumer;

import com.codeguardian.analyzer.model.AnalysisRequest;
import com.codeguardian.analyzer.service.CodeAnalysisService;
import com.codeguardian.analyzer.service.GitService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisRequestConsumer {

    private final CodeAnalysisService analysisService;
    private final GitService gitService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "#{@environment.getProperty('app.kafka.topics.commit-analysis')}")
    public void handleCommitAnalysis(String message) {
        log.info("Received commit analysis request: {}", message);
        
        try {
            // Parse the JSON message from Git Processor
            Map<String, Object> commitData = objectMapper.readValue(message, Map.class);
            
            // Extract commit information
            String commitId = (String) commitData.get("commitId");
            String repositoryName = (String) commitData.get("repositoryName");
            String repositoryUrl = (String) commitData.get("repositoryUrl");
            String repositoryCloneUrl = (String) commitData.get("repositoryCloneUrl");
            String platform = (String) commitData.get("platform");
            
            log.info("Processing commit analysis for: {} in repository: {}", commitId, repositoryName);
            
            // Create analysis request with real code content
            AnalysisRequest request = gitService.createAnalysisRequest(commitData);
            
            // Perform analysis
            analysisService.analyzeCommit(request);
            
            log.info("Successfully processed commit analysis for: {}", commitId);
        } catch (JsonProcessingException e) {
            log.error("Error parsing commit analysis message: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing commit analysis request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{@environment.getProperty('app.kafka.topics.pull-request-analysis')}")
    public void handlePullRequestAnalysis(String message) {
        log.info("Received pull request analysis request: {}", message);
        
        try {
            Map<String, Object> prData = objectMapper.readValue(message, Map.class);
            log.info("Processing pull request analysis for: {}", prData.get("title"));
            
            // Create minimal analysis request for PR
            AnalysisRequest request = createPullRequestAnalysisRequest(prData);
            analysisService.analyzePullRequest(request);
            
            log.info("Successfully processed pull request analysis for: {}", prData.get("prId"));
        } catch (JsonProcessingException e) {
            log.error("Error parsing pull request analysis message: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing pull request analysis request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{@environment.getProperty('app.kafka.topics.merge-request-analysis')}")
    public void handleMergeRequestAnalysis(String message) {
        log.info("Received merge request analysis request: {}", message);
        
        try {
            Map<String, Object> mrData = objectMapper.readValue(message, Map.class);
            log.info("Processing merge request analysis for: {}", mrData.get("title"));
            
            // Create minimal analysis request for MR
            AnalysisRequest request = createMergeRequestAnalysisRequest(mrData);
            analysisService.analyzeMergeRequest(request);
            
            log.info("Successfully processed merge request analysis for: {}", mrData.get("mrId"));
        } catch (JsonProcessingException e) {
            log.error("Error parsing merge request analysis message: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing merge request analysis request: {}", e.getMessage(), e);
        }
    }
    
    private AnalysisRequest createPullRequestAnalysisRequest(Map<String, Object> prData) {
        return new AnalysisRequest(
            (String) prData.get("prId"),
            (String) prData.get("repositoryName"),
            "main",
            "PR-" + prData.get("prId"),
            (String) prData.get("title"),
            "Pull Request Analysis",
            java.time.LocalDateTime.now(),
            java.util.List.of(), // No file changes for PR analysis
            java.util.Map.of()
        );
    }
    
    private AnalysisRequest createMergeRequestAnalysisRequest(Map<String, Object> mrData) {
        return new AnalysisRequest(
            (String) mrData.get("mrId"),
            (String) mrData.get("repositoryName"),
            "main",
            "MR-" + mrData.get("mrId"),
            (String) mrData.get("title"),
            "Merge Request Analysis",
            java.time.LocalDateTime.now(),
            java.util.List.of(), // No file changes for MR analysis
            java.util.Map.of()
        );
    }
}