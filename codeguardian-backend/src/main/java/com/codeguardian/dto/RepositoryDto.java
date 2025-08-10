package com.codeguardian.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class RepositoryDto {
    private Long id;
    private String name;
    private String fullName;
    private String platform;
    private String url;
    private String description;
    private String language;
    private String defaultBranch;
    private boolean isPrivate;
    private Integer commitCount;
    private Integer branchCount;
    private Integer analysisCount;
    private Integer issueCount;
    private String securityStatus;
    private boolean webhookConfigured;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastCommitDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAnalysisDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Default constructor
    public RepositoryDto() {}

    // Constructor with essential fields
    public RepositoryDto(String name, String platform, String url) {
        this.name = name;
        this.platform = platform;
        this.url = url;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Integer getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(Integer commitCount) {
        this.commitCount = commitCount;
    }

    public Integer getBranchCount() {
        return branchCount;
    }

    public void setBranchCount(Integer branchCount) {
        this.branchCount = branchCount;
    }

    public Integer getAnalysisCount() {
        return analysisCount;
    }

    public void setAnalysisCount(Integer analysisCount) {
        this.analysisCount = analysisCount;
    }

    public Integer getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(Integer issueCount) {
        this.issueCount = issueCount;
    }

    public String getSecurityStatus() {
        return securityStatus;
    }

    public void setSecurityStatus(String securityStatus) {
        this.securityStatus = securityStatus;
    }

    public boolean isWebhookConfigured() {
        return webhookConfigured;
    }

    public void setWebhookConfigured(boolean webhookConfigured) {
        this.webhookConfigured = webhookConfigured;
    }

    public LocalDateTime getLastCommitDate() {
        return lastCommitDate;
    }

    public void setLastCommitDate(LocalDateTime lastCommitDate) {
        this.lastCommitDate = lastCommitDate;
    }

    public LocalDateTime getLastAnalysisDate() {
        return lastAnalysisDate;
    }

    public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) {
        this.lastAnalysisDate = lastAnalysisDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "RepositoryDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", platform='" + platform + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                ", defaultBranch='" + defaultBranch + '\'' +
                ", isPrivate=" + isPrivate +
                ", commitCount=" + commitCount +
                ", branchCount=" + branchCount +
                ", analysisCount=" + analysisCount +
                ", issueCount=" + issueCount +
                ", securityStatus='" + securityStatus + '\'' +
                ", webhookConfigured=" + webhookConfigured +
                ", lastCommitDate=" + lastCommitDate +
                ", lastAnalysisDate=" + lastAnalysisDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}