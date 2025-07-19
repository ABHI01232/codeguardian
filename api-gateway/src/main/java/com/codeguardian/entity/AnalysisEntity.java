package com.codeguardian.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyses")
public class AnalysisEntity {

    @Id
    private String id;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(name = "repository_name")
    private String repositoryName;

    @Column(name = "commit_id")
    private String commitId;

    @Column(name = "commit_message")
    private String commitMessage;

    @Column
    private String author;

    @Column(nullable = false)
    private String status;

    @Column
    private String duration;

    @Column
    private LocalDateTime timestamp;

    @Column(name = "critical_findings")
    private Integer criticalFindings = 0;

    @Column(name = "high_findings")
    private Integer highFindings = 0;

    @Column(name = "medium_findings")
    private Integer mediumFindings = 0;

    @Column(name = "low_findings")
    private Integer lowFindings = 0;

    @Column(name = "scan_config", columnDefinition = "TEXT")
    private String scanConfig;

    @Column(name = "findings_details", columnDefinition = "TEXT")
    private String findingsDetails;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AnalysisEntity() {}

    public AnalysisEntity(String id, Long repositoryId, String status) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getRepositoryId() { return repositoryId; }
    public void setRepositoryId(Long repositoryId) { this.repositoryId = repositoryId; }

    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }

    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }

    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getCriticalFindings() { return criticalFindings; }
    public void setCriticalFindings(Integer criticalFindings) { this.criticalFindings = criticalFindings; }

    public Integer getHighFindings() { return highFindings; }
    public void setHighFindings(Integer highFindings) { this.highFindings = highFindings; }

    public Integer getMediumFindings() { return mediumFindings; }
    public void setMediumFindings(Integer mediumFindings) { this.mediumFindings = mediumFindings; }

    public Integer getLowFindings() { return lowFindings; }
    public void setLowFindings(Integer lowFindings) { this.lowFindings = lowFindings; }

    public String getScanConfig() { return scanConfig; }
    public void setScanConfig(String scanConfig) { this.scanConfig = scanConfig; }

    public String getFindingsDetails() { return findingsDetails; }
    public void setFindingsDetails(String findingsDetails) { this.findingsDetails = findingsDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}