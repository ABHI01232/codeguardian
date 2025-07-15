package com.codeguardian.analyzer.consumer;

import com.codeguardian.analyzer.model.AnalysisRequest;
import com.codeguardian.analyzer.service.CodeAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisRequestConsumer {

    private final CodeAnalysisService analysisService;

    @KafkaListener(topics = "#{@environment.getProperty('app.kafka.topics.commit-analysis')}")
    public void handleCommitAnalysis(AnalysisRequest request) {
        log.info("Received commit analysis request for repository: {}, commit: {}", 
                request.getRepositoryUrl(), request.getCommitHash());
        
        try {
            analysisService.analyzeCommit(request);
            log.info("Successfully processed commit analysis for: {}", request.getCommitHash());
        } catch (Exception e) {
            log.error("Error processing commit analysis request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{@environment.getProperty('app.kafka.topics.pull-request-analysis')}")
    public void handlePullRequestAnalysis(AnalysisRequest request) {
        log.info("Received pull request analysis request for repository: {}", request.getRepositoryUrl());
        
        try {
            analysisService.analyzePullRequest(request);
            log.info("Successfully processed pull request analysis for: {}", request.getId());
        } catch (Exception e) {
            log.error("Error processing pull request analysis request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{@environment.getProperty('app.kafka.topics.merge-request-analysis')}")
    public void handleMergeRequestAnalysis(AnalysisRequest request) {
        log.info("Received merge request analysis request for repository: {}", request.getRepositoryUrl());
        
        try {
            analysisService.analyzeMergeRequest(request);
            log.info("Successfully processed merge request analysis for: {}", request.getId());
        } catch (Exception e) {
            log.error("Error processing merge request analysis request: {}", e.getMessage(), e);
        }
    }
}