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
public class SecurityAnalysisService {

    private static final List<SecurityRule> SECURITY_RULES = List.of(
        new SecurityRule("hardcoded_secret", "CRITICAL", 
            Pattern.compile("(?i)(password|secret|key|token)\\s*=\\s*[\"'][^\"']+[\"']"),
            "CWE-798", "Hardcoded credentials detected"),
        new SecurityRule("sql_injection", "HIGH", 
            Pattern.compile("(?i)(select|insert|update|delete).*\\+.*"),
            "CWE-89", "Potential SQL injection vulnerability"),
        new SecurityRule("xss_vulnerability", "HIGH", 
            Pattern.compile("(?i)innerHTML\\s*=\\s*[^;]*\\+"),
            "CWE-79", "Potential XSS vulnerability"),
        new SecurityRule("weak_crypto", "MEDIUM", 
            Pattern.compile("(?i)(md5|sha1)"),
            "CWE-327", "Weak cryptographic algorithm"),
        new SecurityRule("insecure_random", "MEDIUM", 
            Pattern.compile("(?i)math\\.random\\(\\)"),
            "CWE-330", "Insecure random number generation"),
        new SecurityRule("debug_info", "LOW", 
            Pattern.compile("(?i)(console\\.log|print|debug)\\s*\\("),
            "CWE-209", "Debug information exposure")
    );

    public List<AnalysisResult.SecurityIssue> analyzeSecurityIssues(AnalysisRequest request) {
        log.info("Analyzing security issues for commit: {}", request.getCommitHash());
        
        List<AnalysisResult.SecurityIssue> issues = new ArrayList<>();
        
        for (AnalysisRequest.FileChange change : request.getChanges()) {
            if (change.getContent() != null) {
                issues.addAll(analyzeFileForSecurityIssues(change));
            }
        }
        
        log.info("Found {} security issues", issues.size());
        return issues;
    }

    private List<AnalysisResult.SecurityIssue> analyzeFileForSecurityIssues(AnalysisRequest.FileChange file) {
        List<AnalysisResult.SecurityIssue> issues = new ArrayList<>();
        String[] lines = file.getContent().split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            for (SecurityRule rule : SECURITY_RULES) {
                if (rule.pattern.matcher(line).find()) {
                    issues.add(createSecurityIssue(rule, file.getFilename(), i + 1, line));
                }
            }
        }
        
        return issues;
    }

    private AnalysisResult.SecurityIssue createSecurityIssue(SecurityRule rule, String filename, 
                                                           int lineNumber, String codeLine) {
        return new AnalysisResult.SecurityIssue(
            rule.type,
            rule.severity,
            rule.description,
            getDetailedDescription(rule.type),
            filename,
            lineNumber,
            rule.cweId,
            getRecommendation(rule.type),
            codeLine.trim()
        );
    }

    private String getDetailedDescription(String type) {
        return switch (type) {
            case "hardcoded_secret" -> "Hardcoded credentials found in source code. This poses a security risk as credentials should be stored securely.";
            case "sql_injection" -> "Potential SQL injection vulnerability detected. User input may be directly concatenated into SQL queries.";
            case "xss_vulnerability" -> "Potential Cross-Site Scripting (XSS) vulnerability. User input may be directly inserted into DOM.";
            case "weak_crypto" -> "Weak cryptographic algorithm detected. Consider using stronger algorithms like SHA-256 or bcrypt.";
            case "insecure_random" -> "Insecure random number generation. Use cryptographically secure random generators for security-sensitive operations.";
            case "debug_info" -> "Debug information exposure. Remove debug statements before production deployment.";
            default -> "Security issue detected.";
        };
    }

    private String getRecommendation(String type) {
        return switch (type) {
            case "hardcoded_secret" -> "Store credentials in environment variables or secure credential management systems.";
            case "sql_injection" -> "Use parameterized queries or prepared statements to prevent SQL injection.";
            case "xss_vulnerability" -> "Sanitize user input and use safe DOM manipulation methods.";
            case "weak_crypto" -> "Use strong cryptographic algorithms like SHA-256, SHA-3, or bcrypt for hashing.";
            case "insecure_random" -> "Use SecureRandom or equivalent cryptographically secure random number generators.";
            case "debug_info" -> "Remove debug statements and use proper logging frameworks with appropriate log levels.";
            default -> "Review and fix the security issue according to best practices.";
        };
    }

    private static class SecurityRule {
        final String type;
        final String severity;
        final Pattern pattern;
        final String cweId;
        final String description;

        SecurityRule(String type, String severity, Pattern pattern, String cweId, String description) {
            this.type = type;
            this.severity = severity;
            this.pattern = pattern;
            this.cweId = cweId;
            this.description = description;
        }
    }
}