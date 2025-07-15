package com.codeguardian.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String id;
    private String repositoryUrl;
    private String commitHash;
    private String status;
    private LocalDateTime analyzedAt;
    private List<SecurityIssue> securityIssues;
    private List<CodeQualityIssue> codeQualityIssues;
    private ComplianceReport complianceReport;
    private Map<String, Object> metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityIssue {
        private String type;
        private String severity;
        private String title;
        private String description;
        private String filename;
        private int lineNumber;
        private String cweId;
        private String recommendation;
        private String codeSnippet;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeQualityIssue {
        private String type;
        private String severity;
        private String title;
        private String description;
        private String filename;
        private int lineNumber;
        private String rule;
        private String suggestion;
        private String codeSnippet;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceReport {
        private String framework;
        private String overallScore;
        private List<ComplianceRule> rules;
        private List<String> violations;
        private List<String> recommendations;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ComplianceRule {
            private String ruleId;
            private String description;
            private String status;
            private String severity;
        }
    }
}