package com.codeguardian.analyzer.controller;

import com.codeguardian.analyzer.model.AnalysisRequest;
import com.codeguardian.analyzer.service.CodeAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final CodeAnalysisService analysisService;

    @PostMapping("/commit")
    public ResponseEntity<Map<String, String>> analyzeCommit(@RequestBody AnalysisRequest request) {
        log.info("Received commit analysis request for: {}", request.getCommitHash());
        
        try {
            analysisService.analyzeCommit(request);
            return ResponseEntity.ok(Map.of(
                "message", "Analysis request accepted", 
                "commitHash", request.getCommitHash()
            ));
        } catch (Exception e) {
            log.error("Error processing commit analysis request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to process analysis request",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/pull-request")
    public ResponseEntity<Map<String, String>> analyzePullRequest(@RequestBody AnalysisRequest request) {
        log.info("Received pull request analysis request for: {}", request.getId());
        
        try {
            analysisService.analyzePullRequest(request);
            return ResponseEntity.ok(Map.of(
                "message", "Analysis request accepted", 
                "requestId", request.getId()
            ));
        } catch (Exception e) {
            log.error("Error processing pull request analysis request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to process analysis request",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "code-analyzer"
        ));
    }
}