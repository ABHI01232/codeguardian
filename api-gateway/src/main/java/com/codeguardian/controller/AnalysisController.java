package com.codeguardian.controller;

import com.codeguardian.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/analyses")
@CrossOrigin(origins = "http://localhost:3000")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @GetMapping
    public ResponseEntity<List<Object>> getAllAnalyses() {
        List<Object> analyses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAnalysisById(@PathVariable String id) {
        Optional<Object> analysis = analysisService.getAnalysisById(id);
        return analysis.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Object>> getRecentAnalyses(@RequestParam(defaultValue = "10") int limit) {
        List<Object> analyses = analysisService.getRecentAnalyses(limit);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<Object>> getAnalysesByRepository(@PathVariable Long repositoryId) {
        List<Object> analyses = analysisService.getAnalysesByRepository(repositoryId);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Object>> getAnalysesByStatus(@PathVariable String status) {
        List<Object> analyses = analysisService.getAnalysesByStatus(status);
        return ResponseEntity.ok(analyses);
    }

    @PostMapping("/{id}/rerun")
    public ResponseEntity<Map<String, Object>> rerunAnalysis(@PathVariable String id) {
        try {
            Map<String, Object> result = analysisService.rerunAnalysis(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to rerun analysis: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable String id) {
        try {
            analysisService.deleteAnalysis(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAnalysisStatistics() {
        Map<String, Object> statistics = analysisService.getAnalysisStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{id}/findings")
    public ResponseEntity<List<Object>> getAnalysisFindings(@PathVariable String id) {
        List<Object> findings = analysisService.getAnalysisFindings(id);
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/findings/severity/{severity}")
    public ResponseEntity<List<Object>> getFindingsBySeverity(@PathVariable String severity) {
        List<Object> findings = analysisService.getFindingsBySeverity(severity);
        return ResponseEntity.ok(findings);
    }
}