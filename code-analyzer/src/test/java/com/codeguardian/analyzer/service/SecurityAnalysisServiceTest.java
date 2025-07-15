package com.codeguardian.analyzer.service;

import com.codeguardian.analyzer.model.AnalysisRequest;
import com.codeguardian.analyzer.model.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityAnalysisServiceTest {

    private final SecurityAnalysisService securityAnalysisService = new SecurityAnalysisService();

    @Test
    void shouldDetectHardcodedSecrets() {
        // Given
        AnalysisRequest request = createAnalysisRequest(
            "String password = \"hardcoded123\";"
        );

        // When
        List<AnalysisResult.SecurityIssue> issues = securityAnalysisService.analyzeSecurityIssues(request);

        // Then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getType()).isEqualTo("hardcoded_secret");
        assertThat(issues.get(0).getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldDetectSqlInjection() {
        // Given
        AnalysisRequest request = createAnalysisRequest(
            "String query = \"SELECT * FROM users WHERE id = \" + userId;"
        );

        // When
        List<AnalysisResult.SecurityIssue> issues = securityAnalysisService.analyzeSecurityIssues(request);

        // Then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getType()).isEqualTo("sql_injection");
        assertThat(issues.get(0).getSeverity()).isEqualTo("HIGH");
    }

    @Test
    void shouldDetectWeakCrypto() {
        // Given
        AnalysisRequest request = createAnalysisRequest(
            "MessageDigest md = MessageDigest.getInstance(\"MD5\");"
        );

        // When
        List<AnalysisResult.SecurityIssue> issues = securityAnalysisService.analyzeSecurityIssues(request);

        // Then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getType()).isEqualTo("weak_crypto");
        assertThat(issues.get(0).getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    void shouldNotDetectIssuesInCleanCode() {
        // Given
        AnalysisRequest request = createAnalysisRequest(
            "public class CleanCode { public void doSomething() { System.out.println(\"Hello\"); } }"
        );

        // When
        List<AnalysisResult.SecurityIssue> issues = securityAnalysisService.analyzeSecurityIssues(request);

        // Then
        assertThat(issues).isEmpty();
    }

    private AnalysisRequest createAnalysisRequest(String content) {
        AnalysisRequest.FileChange fileChange = new AnalysisRequest.FileChange();
        fileChange.setFilename("test.java");
        fileChange.setContent(content);
        fileChange.setStatus("modified");

        AnalysisRequest request = new AnalysisRequest();
        request.setId("test-request");
        request.setCommitHash("abc123");
        request.setRepositoryUrl("https://github.com/test/repo");
        request.setChanges(List.of(fileChange));

        return request;
    }
}