package com.codeguardian.gitprocessor.consumer;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.repository.CommitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultConsumer {
    private final CommitRepository commitRepository;

    @KafkaListener(topics = "code-analysis-results", groupId = "git-processor-group")
    public void handleAnalysisResult(Map<String, Object> analysisResult) {
        try {
            String commitId = (String) analysisResult.get("commitId");
            String status = (String) analysisResult.get("status");
            String result = (String) analysisResult.get("result");

            log.info("Received analysis result for commit: {}, status: {}", commitId, status);

            CommitEntity commit = commitRepository.findByCommitId(commitId)
                    .orElseThrow(() -> new RuntimeException("Commit not found: " + commitId));

            commit.setAnalysisStatus(status);
            // You might want to store the analysis result in a separate field
            // commit.setAnalysisResult(result);

            commitRepository.save(commit);

            log.info("Updated commit analysis status: {} -> {}", commitId, status);
        } catch (Exception e) {
            log.error("Error processing analysis result: {}", analysisResult, e);
        }
    }
}
