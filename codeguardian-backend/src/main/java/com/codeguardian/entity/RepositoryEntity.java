package com.codeguardian.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_name")
    private String fullName;

    @Column
    private String description;

    @Column(nullable = false)
    private String url;

    @Column(name = "clone_url", nullable = false)
    private String cloneUrl;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String platform;

    @Column
    private String language;

    @Column(name = "default_branch")
    private String defaultBranch = "master";

    @Column(name = "is_private")
    private boolean isPrivate = false;

    @Column(name = "commit_count")
    private Integer commitCount = 0;

    @Column(name = "branch_count")
    private Integer branchCount = 1;

    @Column(name = "analysis_count")
    private Integer analysisCount = 0;

    @Column(name = "issue_count")
    private Integer issueCount = 0;

    @Column(name = "security_status")
    private String securityStatus = "PENDING";

    @Column(name = "webhook_configured")
    private boolean webhookConfigured = false;

    @Column(name = "last_commit_date")
    private LocalDateTime lastCommitDate;

    @Column(name = "last_analysis_date")
    private LocalDateTime lastAnalysisDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RepositoryEntity() {}

    public RepositoryEntity(String name, String platform, String url) {
        this.name = name;
        this.platform = platform;
        this.url = url;
        this.cloneUrl = url; // Use the same URL for cloning
        this.externalId = generateExternalId(url); // Generate unique external ID
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    private String generateExternalId(String url) {
        // Extract owner/repo from URL and use as external ID
        if (url != null) {
            String[] parts = url.replace(".git", "").split("/");
            if (parts.length >= 2) {
                return parts[parts.length - 2] + "/" + parts[parts.length - 1];
            }
        }
        return java.util.UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCloneUrl() { return cloneUrl; }
    public void setCloneUrl(String cloneUrl) { this.cloneUrl = cloneUrl; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public Integer getCommitCount() { return commitCount; }
    public void setCommitCount(Integer commitCount) { this.commitCount = commitCount; }

    public Integer getBranchCount() { return branchCount; }
    public void setBranchCount(Integer branchCount) { this.branchCount = branchCount; }

    public Integer getAnalysisCount() { return analysisCount; }
    public void setAnalysisCount(Integer analysisCount) { this.analysisCount = analysisCount; }

    public Integer getIssueCount() { return issueCount; }
    public void setIssueCount(Integer issueCount) { this.issueCount = issueCount; }

    public String getSecurityStatus() { return securityStatus; }
    public void setSecurityStatus(String securityStatus) { this.securityStatus = securityStatus; }

    public boolean isWebhookConfigured() { return webhookConfigured; }
    public void setWebhookConfigured(boolean webhookConfigured) { this.webhookConfigured = webhookConfigured; }

    public LocalDateTime getLastCommitDate() { return lastCommitDate; }
    public void setLastCommitDate(LocalDateTime lastCommitDate) { this.lastCommitDate = lastCommitDate; }

    public LocalDateTime getLastAnalysisDate() { return lastAnalysisDate; }
    public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) { this.lastAnalysisDate = lastAnalysisDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}