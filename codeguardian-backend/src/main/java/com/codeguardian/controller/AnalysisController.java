package com.codeguardian.controller;

import com.codeguardian.entity.AnalysisEntity;
import com.codeguardian.entity.RepositoryEntity;
import com.codeguardian.repository.AnalysisEntityRepository;
import com.codeguardian.repository.RepositoryEntityRepository;
import com.codeguardian.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.kafka.core.KafkaTemplate;

@RestController
@RequestMapping("/api/analyses")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Security Analysis", description = "APIs for managing and viewing security analysis results")
public class AnalysisController {

    @Autowired
    private AnalysisEntityRepository analysisEntityRepository;
    
    @Autowired
    private RepositoryEntityRepository repositoryEntityRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all analyses", description = "Retrieve all security analysis reports")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved analyses")
    public ResponseEntity<List<AnalysisEntity>> getAllAnalyses() {
        List<AnalysisEntity> analyses = analysisEntityRepository.findAllOrderByTimestampDesc();
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisEntity> getAnalysisById(@PathVariable String id) {
        Optional<AnalysisEntity> analysis = analysisEntityRepository.findById(id);
        return analysis.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AnalysisEntity>> getRecentAnalyses(@RequestParam(defaultValue = "10") int limit) {
        List<AnalysisEntity> recentAnalyses = analysisEntityRepository.findAllOrderByTimestampDesc()
            .stream()
            .limit(limit)
            .toList();
        return ResponseEntity.ok(recentAnalyses);
    }

    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<AnalysisEntity>> getAnalysesByRepository(@PathVariable Long repositoryId) {
        List<AnalysisEntity> repoAnalyses = analysisEntityRepository.findByRepositoryIdOrderByTimestampDesc(repositoryId);
        return ResponseEntity.ok(repoAnalyses);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get analysis statistics", description = "Retrieve aggregate statistics for all security analyses")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    public ResponseEntity<Map<String, Object>> getAnalysisStatistics() {
        List<AnalysisEntity> allAnalyses = analysisEntityRepository.findAll();
        long totalAnalyses = allAnalyses.size();
        long completedAnalyses = analysisEntityRepository.countByStatus("COMPLETED");
        
        // Calculate totals from actual database data
        int totalCritical = allAnalyses.stream().mapToInt(a -> a.getCriticalFindings() != null ? a.getCriticalFindings() : 0).sum();
        int totalHigh = allAnalyses.stream().mapToInt(a -> a.getHighFindings() != null ? a.getHighFindings() : 0).sum();
        int totalMedium = allAnalyses.stream().mapToInt(a -> a.getMediumFindings() != null ? a.getMediumFindings() : 0).sum();
        int totalLow = allAnalyses.stream().mapToInt(a -> a.getLowFindings() != null ? a.getLowFindings() : 0).sum();
        int riskScore = totalCritical * 10 + totalHigh * 7 + totalMedium * 4 + totalLow * 1;
        
        Map<String, Object> stats = Map.of(
            "totalAnalyses", totalAnalyses,
            "completedAnalyses", completedAnalyses,
            "criticalFindings", totalCritical,
            "highFindings", totalHigh,
            "mediumFindings", totalMedium,
            "lowFindings", totalLow,
            "riskScore", riskScore
        );
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{id}/rerun")
    @Operation(summary = "Rerun analysis", description = "Trigger a new analysis run for existing analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analysis rerun started"),
        @ApiResponse(responseCode = "400", description = "Failed to rerun analysis")
    })
    public ResponseEntity<Map<String, Object>> rerunAnalysis(
            @Parameter(description = "Analysis ID", required = true)
            @PathVariable String id) {
        Optional<AnalysisEntity> existingAnalysis = analysisEntityRepository.findById(id);
        if (existingAnalysis.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Analysis not found", "analysisId", id));
        }
        
        // Update status to RUNNING
        AnalysisEntity analysis = existingAnalysis.get();
        analysis.setStatus("RUNNING");
        analysis.setUpdatedAt(LocalDateTime.now());
        analysisEntityRepository.save(analysis);
        
        // Get repository details
        Optional<RepositoryEntity> repository = repositoryEntityRepository.findById(analysis.getRepositoryId());
        if (repository.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Repository not found for analysis", "analysisId", id));
        }
        
        RepositoryEntity repo = repository.get();
        
        // Trigger actual analysis rerun via Kafka
        String newAnalysisId = "rerun-" + id + "-" + System.currentTimeMillis();
        Map<String, Object> analysisRequest = Map.of(
            "analysisId", newAnalysisId,
            "repositoryId", repo.getId(),
            "repositoryName", repo.getName(),
            "repositoryUrl", repo.getUrl(),
            "rerunOf", id
        );
        
        try {
            kafkaTemplate.send("analysis-requests", newAnalysisId, analysisRequest);
            System.out.println("📤 Analysis rerun request sent to Kafka: " + newAnalysisId);
            
            // Send notification
            notificationService.sendWebhookReceived(repo.getName(), "RERUN");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending rerun request to Kafka: " + e.getMessage());
            analysis.setStatus("FAILED");
            analysisEntityRepository.save(analysis);
            
            // Send error notification
            notificationService.sendErrorNotification("Kafka Error", "Failed to trigger analysis rerun", repo.getName());
            
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to trigger analysis rerun", "analysisId", id));
        }
        
        return ResponseEntity.ok(Map.of("message", "Analysis rerun started", "analysisId", id));
    }
}