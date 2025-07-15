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
public class CodeQualityAnalysisService {

    private static final List<QualityRule> QUALITY_RULES = List.of(
        new QualityRule("long_method", "MEDIUM", 
            Pattern.compile("(?s)\\{[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n[^}]*\\n"),
            "Method is too long and should be refactored"),
        new QualityRule("magic_number", "LOW", 
            Pattern.compile("\\b\\d{2,}\\b(?!\\s*[)]}])"),
            "Magic number detected, consider using named constants"),
        new QualityRule("todo_comment", "LOW", 
            Pattern.compile("(?i)(todo|fixme|hack)\\s*:"),
            "TODO comment found, consider addressing before production"),
        new QualityRule("empty_catch", "HIGH", 
            Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}"),
            "Empty catch block detected"),
        new QualityRule("unused_import", "LOW", 
            Pattern.compile("^import\\s+[^;]+;\\s*$"),
            "Potentially unused import"),
        new QualityRule("long_parameter_list", "MEDIUM", 
            Pattern.compile("\\([^)]*,\\s*[^)]*,\\s*[^)]*,\\s*[^)]*,\\s*[^)]*,"),
            "Method has too many parameters")
    );

    public List<AnalysisResult.CodeQualityIssue> analyzeCodeQuality(AnalysisRequest request) {
        log.info("Analyzing code quality for commit: {}", request.getCommitHash());
        
        List<AnalysisResult.CodeQualityIssue> issues = new ArrayList<>();
        
        for (AnalysisRequest.FileChange change : request.getChanges()) {
            if (change.getContent() != null) {
                issues.addAll(analyzeFileForQualityIssues(change));
            }
        }
        
        log.info("Found {} code quality issues", issues.size());
        return issues;
    }

    private List<AnalysisResult.CodeQualityIssue> analyzeFileForQualityIssues(AnalysisRequest.FileChange file) {
        List<AnalysisResult.CodeQualityIssue> issues = new ArrayList<>();
        String[] lines = file.getContent().split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            for (QualityRule rule : QUALITY_RULES) {
                if (rule.pattern.matcher(line).find()) {
                    issues.add(createQualityIssue(rule, file.getFilename(), i + 1, line));
                }
            }
        }
        
        // Check for method complexity
        issues.addAll(checkMethodComplexity(file));
        
        return issues;
    }

    private List<AnalysisResult.CodeQualityIssue> checkMethodComplexity(AnalysisRequest.FileChange file) {
        List<AnalysisResult.CodeQualityIssue> issues = new ArrayList<>();
        String content = file.getContent();
        
        // Simple complexity check based on cyclomatic complexity indicators
        String[] lines = content.split("\n");
        int complexity = 0;
        int methodStartLine = -1;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // Detect method start
            if (line.matches(".*\\b(public|private|protected)\\b.*\\([^)]*\\).*\\{.*")) {
                methodStartLine = i + 1;
                complexity = 1; // Base complexity
            }
            
            // Count complexity indicators
            if (line.contains("if") || line.contains("else") || line.contains("while") || 
                line.contains("for") || line.contains("switch") || line.contains("case") ||
                line.contains("catch") || line.contains("&&") || line.contains("||")) {
                complexity++;
            }
            
            // Method end
            if (line.contains("}") && methodStartLine != -1 && complexity > 10) {
                issues.add(new AnalysisResult.CodeQualityIssue(
                    "high_complexity",
                    "HIGH",
                    "High cyclomatic complexity",
                    String.format("Method has cyclomatic complexity of %d (threshold: 10)", complexity),
                    file.getFilename(),
                    methodStartLine,
                    "complexity",
                    "Break down the method into smaller, more focused methods",
                    "Method complexity: " + complexity
                ));
                methodStartLine = -1;
                complexity = 0;
            }
        }
        
        return issues;
    }

    private AnalysisResult.CodeQualityIssue createQualityIssue(QualityRule rule, String filename, 
                                                             int lineNumber, String codeLine) {
        return new AnalysisResult.CodeQualityIssue(
            rule.type,
            rule.severity,
            rule.description,
            getDetailedDescription(rule.type),
            filename,
            lineNumber,
            rule.type,
            getSuggestion(rule.type),
            codeLine.trim()
        );
    }

    private String getDetailedDescription(String type) {
        return switch (type) {
            case "long_method" -> "Method is too long and may be difficult to understand and maintain.";
            case "magic_number" -> "Magic numbers make code less readable and maintainable.";
            case "todo_comment" -> "TODO comments indicate incomplete or temporary code.";
            case "empty_catch" -> "Empty catch blocks suppress exceptions and can hide errors.";
            case "unused_import" -> "Unused imports clutter the code and may indicate dead code.";
            case "long_parameter_list" -> "Methods with many parameters are difficult to use and understand.";
            default -> "Code quality issue detected.";
        };
    }

    private String getSuggestion(String type) {
        return switch (type) {
            case "long_method" -> "Break the method into smaller, more focused methods.";
            case "magic_number" -> "Replace magic numbers with named constants or configuration.";
            case "todo_comment" -> "Complete the TODO item or create a proper issue tracker entry.";
            case "empty_catch" -> "Add proper error handling or logging in the catch block.";
            case "unused_import" -> "Remove unused imports to clean up the code.";
            case "long_parameter_list" -> "Consider using a parameter object or breaking down the method.";
            default -> "Review and improve the code quality according to best practices.";
        };
    }

    private static class QualityRule {
        final String type;
        final String severity;
        final Pattern pattern;
        final String description;

        QualityRule(String type, String severity, Pattern pattern, String description) {
            this.type = type;
            this.severity = severity;
            this.pattern = pattern;
            this.description = description;
        }
    }
}