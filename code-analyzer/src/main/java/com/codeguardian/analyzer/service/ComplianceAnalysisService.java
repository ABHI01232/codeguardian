package com.codeguardian.analyzer.service;

import com.codeguardian.analyzer.model.AnalysisRequest;
import com.codeguardian.analyzer.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ComplianceAnalysisService {

    private static final List<ComplianceRule> COMPLIANCE_RULES = List.of(
        new ComplianceRule("PCI-DSS-1", "Protect cardholder data", 
            Pattern.compile("(?i)(credit_card|card_number|cvv|pan)"), "CRITICAL"),
        new ComplianceRule("GDPR-1", "Personal data protection", 
            Pattern.compile("(?i)(email|phone|address|ssn|personal_id)"), "HIGH"),
        new ComplianceRule("HIPAA-1", "Protected health information", 
            Pattern.compile("(?i)(medical_record|patient_id|health_info)"), "HIGH"),
        new ComplianceRule("SOX-1", "Financial data integrity", 
            Pattern.compile("(?i)(financial_record|audit_trail|transaction)"), "MEDIUM"),
        new ComplianceRule("OWASP-1", "Secure coding practices", 
            Pattern.compile("(?i)(password|authentication|authorization)"), "MEDIUM")
    );

    public AnalysisResult.ComplianceReport analyzeCompliance(AnalysisRequest request) {
        log.info("Analyzing compliance for commit: {}", request.getCommitHash());
        
        List<AnalysisResult.ComplianceReport.ComplianceRule> rules = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        for (AnalysisRequest.FileChange change : request.getChanges()) {
            if (change.getContent() != null) {
                analyzeFileForCompliance(change, rules, violations, recommendations);
            }
        }
        
        String overallScore = calculateOverallScore(rules);
        
        log.info("Compliance analysis completed. Overall score: {}", overallScore);
        
        return new AnalysisResult.ComplianceReport(
            "Multi-Framework",
            overallScore,
            rules,
            violations,
            recommendations
        );
    }

    private void analyzeFileForCompliance(AnalysisRequest.FileChange file, 
                                        List<AnalysisResult.ComplianceReport.ComplianceRule> rules,
                                        List<String> violations, 
                                        List<String> recommendations) {
        String content = file.getContent();
        
        for (ComplianceRule rule : COMPLIANCE_RULES) {
            if (rule.pattern.matcher(content).find()) {
                String status = "VIOLATION";
                violations.add(String.format("%s: %s in file %s", 
                    rule.ruleId, rule.description, file.getFilename()));
                
                switch (rule.ruleId) {
                    case "PCI-DSS-1":
                        recommendations.add("Implement encryption for cardholder data storage and transmission");
                        break;
                    case "GDPR-1":
                        recommendations.add("Ensure personal data is processed lawfully and securely");
                        break;
                    case "HIPAA-1":
                        recommendations.add("Implement access controls and audit trails for health information");
                        break;
                    case "SOX-1":
                        recommendations.add("Maintain proper audit trails for financial transactions");
                        break;
                    case "OWASP-1":
                        recommendations.add("Follow OWASP secure coding guidelines");
                        break;
                }
                
                rules.add(new AnalysisResult.ComplianceReport.ComplianceRule(
                    rule.ruleId, rule.description, status, rule.severity));
            } else {
                rules.add(new AnalysisResult.ComplianceReport.ComplianceRule(
                    rule.ruleId, rule.description, "COMPLIANT", rule.severity));
            }
        }
    }

    private String calculateOverallScore(List<AnalysisResult.ComplianceReport.ComplianceRule> rules) {
        long compliantRules = rules.stream()
            .filter(rule -> "COMPLIANT".equals(rule.getStatus()))
            .count();
        
        if (rules.isEmpty()) {
            return "N/A";
        }
        
        double percentage = (double) compliantRules / rules.size() * 100;
        
        if (percentage >= 90) {
            return "EXCELLENT";
        } else if (percentage >= 75) {
            return "GOOD";
        } else if (percentage >= 50) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    private static class ComplianceRule {
        final String ruleId;
        final String description;
        final Pattern pattern;
        final String severity;

        ComplianceRule(String ruleId, String description, Pattern pattern, String severity) {
            this.ruleId = ruleId;
            this.description = description;
            this.pattern = pattern;
            this.severity = severity;
        }
    }
}