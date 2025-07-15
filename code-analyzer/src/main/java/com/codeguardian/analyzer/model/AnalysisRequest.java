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
public class AnalysisRequest {
    private String id;
    private String repositoryUrl;
    private String branch;
    private String commitHash;
    private String commitMessage;
    private String author;
    private LocalDateTime timestamp;
    private List<FileChange> changes;
    private Map<String, Object> metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileChange {
        private String filename;
        private String status;
        private String content;
        private String previousContent;
        private int additions;
        private int deletions;
        private String patch;
    }
}