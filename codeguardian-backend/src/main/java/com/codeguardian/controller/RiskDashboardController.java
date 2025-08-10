package com.codeguardian.controller;

import com.codeguardian.service.RiskAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/risk-dashboard")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Risk Dashboard", description = "APIs for security risk analysis, compliance scoring, and executive dashboards")
public class RiskDashboardController {

    @Autowired
    private RiskAnalysisService riskAnalysisService;

    @GetMapping("/overview")
    @Operation(summary = "Get security risk overview", 
               description = "Comprehensive security risk overview across all repositories")
    @ApiResponse(responseCode = "200", description = "Risk overview retrieved successfully")
    public ResponseEntity<Map<String, Object>> getRiskOverview() {
        Map<String, Object> overview = riskAnalysisService.calculateGlobalRiskOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/repository/{repositoryId}/risk-score")
    @Operation(summary = "Get repository risk score", 
               description = "Detailed risk scoring for a specific repository")
    public ResponseEntity<Map<String, Object>> getRepositoryRiskScore(
            @Parameter(description = "Repository ID", required = true)
            @PathVariable Long repositoryId) {
        Map<String, Object> riskScore = riskAnalysisService.calculateRepositoryRisk(repositoryId);
        return ResponseEntity.ok(riskScore);
    }

    @GetMapping("/compliance/owasp-top10")
    @Operation(summary = "OWASP Top 10 compliance report", 
               description = "Analyze findings against OWASP Top 10 vulnerabilities")
    public ResponseEntity<Map<String, Object>> getOwaspTop10Compliance() {
        Map<String, Object> compliance = riskAnalysisService.generateOwaspTop10Report();
        return ResponseEntity.ok(compliance);
    }

    @GetMapping("/compliance/banking")
    @Operation(summary = "Banking security compliance", 
               description = "Banking industry specific security compliance analysis")
    public ResponseEntity<Map<String, Object>> getBankingCompliance() {
        Map<String, Object> compliance = riskAnalysisService.generateBankingComplianceReport();
        return ResponseEntity.ok(compliance);
    }

    @GetMapping("/trends")
    @Operation(summary = "Security risk trends", 
               description = "Historical risk trends and analytics")
    public ResponseEntity<Map<String, Object>> getSecurityTrends(
            @Parameter(description = "Number of days to analyze", required = false)
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> trends = riskAnalysisService.calculateSecurityTrends(days);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/executive-summary")
    @Operation(summary = "Executive risk summary", 
               description = "High-level executive summary for management reporting")
    public ResponseEntity<Map<String, Object>> getExecutiveSummary() {
        Map<String, Object> summary = riskAnalysisService.generateExecutiveSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/repository/{repositoryId}/remediation-priority")
    @Operation(summary = "Get remediation priorities", 
               description = "Prioritized list of security issues to fix first")
    public ResponseEntity<Map<String, Object>> getRemediationPriorities(
            @Parameter(description = "Repository ID", required = true)
            @PathVariable Long repositoryId) {
        Map<String, Object> priorities = riskAnalysisService.calculateRemediationPriorities(repositoryId);
        return ResponseEntity.ok(priorities);
    }

    @GetMapping("/risk-matrix")
    @Operation(summary = "Security risk matrix", 
               description = "Risk assessment matrix showing likelihood vs impact")
    public ResponseEntity<Map<String, Object>> getRiskMatrix() {
        Map<String, Object> matrix = riskAnalysisService.generateRiskMatrix();
        return ResponseEntity.ok(matrix);
    }
}