package com.codeguardian.analyzer.service;

import com.codeguardian.analyzer.model.AnalysisRequest;
import com.codeguardian.analyzer.model.AnalysisResult;
import com.codeguardian.analyzer.producer.AnalysisResultProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeAnalysisService {

    private final SecurityAnalysisService securityAnalysisService;
    private final CodeQualityAnalysisService codeQualityAnalysisService;
    private final ComplianceAnalysisService complianceAnalysisService;
    private final AnalysisResultProducer resultProducer;

    public void analyzeCommit(AnalysisRequest request) {
        log.info("Starting commit analysis for: {}", request.getCommitHash());
        
        CompletableFuture.runAsync(() -> {
            try {
                AnalysisResult result = performAnalysis(request);
                resultProducer.sendResult(result);
            } catch (Exception e) {
                log.error("Error during commit analysis: {}", e.getMessage(), e);
                sendErrorResult(request, e);
            }
        });
    }

    public void analyzePullRequest(AnalysisRequest request) {
        log.info("Starting pull request analysis for: {}", request.getId());
        
        CompletableFuture.runAsync(() -> {
            try {
                AnalysisResult result = performAnalysis(request);
                resultProducer.sendResult(result);
            } catch (Exception e) {
                log.error("Error during pull request analysis: {}", e.getMessage(), e);
                sendErrorResult(request, e);
            }
        });
    }

    public void analyzeMergeRequest(AnalysisRequest request) {
        log.info("Starting merge request analysis for: {}", request.getId());
        
        CompletableFuture.runAsync(() -> {
            try {
                AnalysisResult result = performAnalysis(request);
                resultProducer.sendResult(result);
            } catch (Exception e) {
                log.error("Error during merge request analysis: {}", e.getMessage(), e);
                sendErrorResult(request, e);
            }
        });
    }

    private AnalysisResult performAnalysis(AnalysisRequest request) {
        log.info("Performing comprehensive analysis for: {}", request.getCommitHash());

        List<AnalysisResult.SecurityIssue> securityIssues = new ArrayList<>();
        List<AnalysisResult.CodeQualityIssue> codeQualityIssues = new ArrayList<>();
        AnalysisResult.ComplianceReport complianceReport = null;
        Map<String, Object> metrics = new HashMap<>();

        try {
            securityIssues = securityAnalysisService.analyzeSecurityIssues(request);
            codeQualityIssues = codeQualityAnalysisService.analyzeCodeQuality(request);
            complianceReport = complianceAnalysisService.analyzeCompliance(request);
            metrics = calculateMetrics(request, securityIssues, codeQualityIssues);
        } catch (Exception e) {
            log.error("Error during analysis: {}", e.getMessage(), e);
        }

        return new AnalysisResult(
            request.getId(),
            request.getRepositoryUrl(),
            request.getCommitHash(),
            "COMPLETED",
            LocalDateTime.now(),
            securityIssues,
            codeQualityIssues,
            complianceReport,
            metrics
        );
    }

    private void sendErrorResult(AnalysisRequest request, Exception e) {
        AnalysisResult errorResult = new AnalysisResult(
            request.getId(),
            request.getRepositoryUrl(),
            request.getCommitHash(),
            "ERROR",
            LocalDateTime.now(),
            new ArrayList<>(),
            new ArrayList<>(),
            null,
            Map.of("error", e.getMessage())
        );
        
        resultProducer.sendResult(errorResult);
    }

    private Map<String, Object> calculateMetrics(AnalysisRequest request, 
                                               List<AnalysisResult.SecurityIssue> securityIssues,
                                               List<AnalysisResult.CodeQualityIssue> codeQualityIssues) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalFiles", request.getChanges().size());
        metrics.put("totalSecurityIssues", securityIssues.size());
        metrics.put("totalCodeQualityIssues", codeQualityIssues.size());
        metrics.put("criticalSecurityIssues", securityIssues.stream()
            .filter(issue -> "CRITICAL".equals(issue.getSeverity()))
            .count());
        metrics.put("highSecurityIssues", securityIssues.stream()
            .filter(issue -> "HIGH".equals(issue.getSeverity()))
            .count());
        metrics.put("analysisTimestamp", LocalDateTime.now());
        
        return metrics;
    }
}