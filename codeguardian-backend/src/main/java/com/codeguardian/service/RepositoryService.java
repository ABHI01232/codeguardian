package com.codeguardian.service;

import com.codeguardian.dto.RepositoryDto;
import com.codeguardian.entity.RepositoryEntity;
import com.codeguardian.repository.RepositoryEntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

@Service
@Transactional
public class RepositoryService {

    @Autowired
    private RepositoryEntityRepository repositoryRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SecurityAnalysisService securityAnalysisService;
    
    @Autowired(required = false)  // Optional in case Kafka fails to start
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostConstruct
    public void initializeService() {
        System.out.println("✅ RepositoryService initialized - ready to handle repositories");
    }
    
    private void validateRepositoryDto(RepositoryDto repositoryDto) {
        if (repositoryDto.getUrl() == null || repositoryDto.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Repository URL is required");
        }
        
        String url = repositoryDto.getUrl().trim();
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            throw new IllegalArgumentException("Repository URL must start with http:// or https://");
        }
        
        if (!url.contains("github.com") && !url.contains("gitlab.com")) {
            throw new IllegalArgumentException("Only GitHub and GitLab repositories are supported");
        }
        
        if (repositoryDto.getName() == null || repositoryDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name is required");
        }
    }
    
    private void fetchRepositoryBranchesAsync(Long repositoryId, String repositoryUrl) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Simulate GitHub API call to get default branch
                String defaultBranch = detectDefaultBranch(repositoryUrl);
                
                // Update repository with detected branch
                RepositoryEntity repository = repositoryRepository.findById(repositoryId).orElse(null);
                if (repository != null) {
                    repository.setDefaultBranch(defaultBranch);
                    repositoryRepository.save(repository);
                    System.out.println("✅ Updated default branch for repository " + repositoryId + " to: " + defaultBranch);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Failed to detect default branch for repository " + repositoryId + ": " + e.getMessage());
            }
        });
    }
    
    private String detectDefaultBranch(String repositoryUrl) {
        try {
            // Extract owner/repo from URL
            String[] parts = repositoryUrl.replace(".git", "").split("/");
            if (parts.length < 2) return "main";
            
            String owner = parts[parts.length - 2];
            String repo = parts[parts.length - 1];
            
            // Use git ls-remote to detect default branch
            ProcessBuilder pb = new ProcessBuilder("git", "ls-remote", "--symref", repositoryUrl, "HEAD");
            Process process = pb.start();
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.startsWith("ref: refs/heads/")) {
                    return line.substring("ref: refs/heads/".length());
                }
            }
            
            // Fallback to common defaults
            return "main";
        } catch (Exception e) {
            return "main";
        }
    }

    public List<RepositoryDto> getAllRepositories() {
        List<RepositoryEntity> entities = repositoryRepository.findAllOrderByCreatedAtDesc();
        return entities.stream().map(this::convertToDto).toList();
    }

    public Optional<RepositoryDto> getRepositoryById(Long id) {
        return repositoryRepository.findById(id).map(this::convertToDto);
    }

    public RepositoryDto createRepository(RepositoryDto repositoryDto) {
        // Validate repository data
        validateRepositoryDto(repositoryDto);
        
        RepositoryEntity entity = convertToEntity(repositoryDto);
        
        // Set default values
        if (entity.getCommitCount() == null) entity.setCommitCount(0);
        if (entity.getBranchCount() == null) entity.setBranchCount(1);
        if (entity.getAnalysisCount() == null) entity.setAnalysisCount(0);
        if (entity.getIssueCount() == null) entity.setIssueCount(0);
        if (entity.getSecurityStatus() == null) entity.setSecurityStatus("PENDING");
        if (entity.getDefaultBranch() == null) entity.setDefaultBranch("main");
        
        // Auto-generate fullName if not provided
        if (entity.getFullName() == null && entity.getUrl() != null) {
            entity.setFullName(extractFullNameFromUrl(entity.getUrl()));
        }
        
        // Set clone URL (same as URL for now)
        if (entity.getCloneUrl() == null && entity.getUrl() != null) {
            entity.setCloneUrl(entity.getUrl());
        }
        
        // Set external ID if not provided
        if (entity.getExternalId() == null && entity.getUrl() != null) {
            entity.setExternalId(extractExternalIdFromUrl(entity.getUrl()));
        }
        
        RepositoryEntity savedEntity = repositoryRepository.save(entity);
        RepositoryDto savedDto = convertToDto(savedEntity);
        
        // Send notification via Kafka
        sendRepositoryNotification("repository_created", savedDto);
        
        // Trigger async branch detection
        fetchRepositoryBranchesAsync(savedDto.getId(), entity.getUrl());
        
        return savedDto;
    }

    public RepositoryDto updateRepository(Long id, RepositoryDto repositoryDto) {
        RepositoryEntity existing = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + id));
        
        // Update fields
        if (repositoryDto.getName() != null) existing.setName(repositoryDto.getName());
        if (repositoryDto.getDescription() != null) existing.setDescription(repositoryDto.getDescription());
        if (repositoryDto.getLanguage() != null) existing.setLanguage(repositoryDto.getLanguage());
        if (repositoryDto.getDefaultBranch() != null) existing.setDefaultBranch(repositoryDto.getDefaultBranch());
        existing.setPrivate(repositoryDto.isPrivate());
        existing.setWebhookConfigured(repositoryDto.isWebhookConfigured());
        
        RepositoryEntity savedEntity = repositoryRepository.save(existing);
        RepositoryDto savedDto = convertToDto(savedEntity);
        
        // Send notification via Kafka
        sendRepositoryNotification("repository_updated", savedDto);
        
        return savedDto;
    }

    public void deleteRepository(Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + id));
        
        RepositoryDto repoDto = convertToDto(repository);
        repositoryRepository.delete(repository);
        
        // Send notification via Kafka
        sendRepositoryNotification("repository_deleted", repoDto);
    }

    public String triggerAnalysis(Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + id));
        
        String analysisId = "analysis-" + System.currentTimeMillis();
        System.out.println("🔍 Triggering analysis for repository: " + repository.getName());
        
        // Try async processing with Kafka first
        if (kafkaTemplate != null) {
            sendAnalysisRequestToKafka(repository, analysisId);
        } else {
            // Fallback to synchronous processing
            System.out.println("⚠️ Kafka not available, running synchronous analysis...");
            performSecurityAnalysis(repository, analysisId);
        }
        
        return analysisId;
    }
    
    private void sendAnalysisRequestToKafka(RepositoryEntity repository, String analysisId) {
        try {
            Map<String, Object> analysisRequest = Map.of(
                "analysisId", analysisId,
                "repositoryId", repository.getId(),
                "repositoryName", repository.getName(),
                "repositoryUrl", repository.getUrl(),
                "timestamp", System.currentTimeMillis()
            );
            
            kafkaTemplate.send("analysis-requests", analysisId, analysisRequest);
            System.out.println("📨 Analysis request sent to Kafka topic: analysis-requests");
            System.out.println("   • Analysis ID: " + analysisId);
            System.out.println("   • Repository: " + repository.getName());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send to Kafka, falling back to sync: " + e.getMessage());
            performSecurityAnalysis(repository, analysisId);
        }
    }
    
    private void performSecurityAnalysis(RepositoryEntity repository, String analysisId) {
        // Perform actual security analysis using the dedicated service
        Map<String, Object> analysisResult = securityAnalysisService.performSecurityAnalysis(
            repository.getName(), 
            repository.getUrl()
        );
        
        // In a real application, you would store the analysis results in database
        // For now, just log the results
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) analysisResult.get("summary");
        System.out.println("📊 Analysis Summary for " + repository.getName() + ":");
        System.out.println("   • Total Findings: " + summary.get("totalFindings"));
        System.out.println("   • Critical: " + summary.get("criticalCount"));
        System.out.println("   • High: " + summary.get("highCount"));
        System.out.println("   • Medium: " + summary.get("mediumCount"));
        System.out.println("   • Low: " + summary.get("lowCount"));
        System.out.println("   • Risk Score: " + summary.get("riskScore"));
    }

    public List<Object> getRepositoryAnalyses(Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id).orElse(null);
        if (repository == null) {
            return new ArrayList<>();
        }
        
        // Return mock analysis data for now
        List<Object> analyses = new ArrayList<>();
        analyses.add(Map.of(
            "id", "analysis-" + System.currentTimeMillis(),
            "repositoryName", repository.getName(),
            "status", "COMPLETED",
            "findings", 5,
            "riskScore", 42
        ));
        return analyses;
    }

    public List<Object> getRepositoryCommits(Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id).orElse(null);
        if (repository == null) {
            System.out.println("❌ Repository not found with ID: " + id);
            return new ArrayList<>();
        }
        
        System.out.println("🔍 [NEW CODE] Fetching commits for repository " + id + " (" + repository.getName() + ")");
        
        try {
            // Try to fetch real commits first
            List<Object> realCommits = fetchCommitsFromRepository(repository);
            if (!realCommits.isEmpty()) {
                // Update repository stats with real commits
                updateRepositoryCommitStats(repository, realCommits);
                return realCommits;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to fetch real commits for repository " + id + ": " + e.getMessage());
        }
        
        // Always return mock commits for demonstration
        System.out.println("📝 Returning mock commits for repository " + id);
        List<Object> mockCommits = createMockCommits(repository);
        
        // Make sure the repository stats are updated in the database
        // Re-fetch repository to ensure we have latest version
        System.out.println("🔧 Attempting to update repository stats for ID: " + id);
        RepositoryEntity freshRepository = repositoryRepository.findById(id).orElse(null);
        if (freshRepository != null) {
            System.out.println("🔧 Fresh repository found, updating stats...");
            updateRepositoryCommitStats(freshRepository, mockCommits);
        } else {
            System.out.println("❌ Fresh repository not found for ID: " + id);
        }
        
        return mockCommits;
    }
    
    public void updateRepositoryStats(Long id) {
        System.out.println("🔧 Manual update repository stats for ID: " + id);
        
        RepositoryEntity repository = repositoryRepository.findById(id).orElse(null);
        if (repository == null) {
            throw new RuntimeException("Repository not found with ID: " + id);
        }
        
        // Get commits and update stats
        List<Object> commits = createMockCommits(repository);
        System.out.println("🔧 Got " + commits.size() + " commits, now updating stats...");
        
        updateRepositoryCommitStats(repository, commits);
        System.out.println("✅ Repository stats update completed for ID: " + id);
    }
    
    private List<Object> fetchCommitsFromRepository(RepositoryEntity repository) {
        try {
            String repositoryUrl = repository.getUrl();
            if (repositoryUrl == null) {
                return new ArrayList<>();
            }
            
            // Use git ls-remote to get the latest commits from the remote repository
            ProcessBuilder processBuilder = new ProcessBuilder(
                "git", "ls-remote", "--heads", repositoryUrl
            );
            
            Process process = processBuilder.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            
            List<Object> commits = new ArrayList<>();
            String line;
            int commitCount = 0;
            
            while ((line = reader.readLine()) != null && commitCount < 10) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String commitId = parts[0];
                    String ref = parts[1];
                    String branchName = ref.replace("refs/heads/", "");
                    
                    Map<String, Object> commit = new HashMap<>();
                    commit.put("id", commitId);
                    commit.put("message", "Latest commit on " + branchName);
                    commit.put("author", "Unknown");
                    commit.put("timestamp", new Date().toString());
                    commit.put("branch", branchName);
                    commit.put("analysisStatus", "PENDING");
                    commit.put("issueCount", 0);
                    
                    commits.add(commit);
                    commitCount++;
                }
            }
            
            process.waitFor();
            reader.close();
            
            System.out.println("✅ Fetched " + commits.size() + " commits for repository " + repository.getId());
            return commits;
            
        } catch (Exception e) {
            System.out.println("❌ Error fetching commits: " + e.getMessage());
            throw new RuntimeException("Failed to fetch commits", e);
        }
    }
    
    private List<Object> createMockCommits(RepositoryEntity repository) {
        List<Object> commits = new ArrayList<>();
        
        // Create a few mock commits to show in the UI
        for (int i = 0; i < 5; i++) {
            Map<String, Object> commit = new HashMap<>();
            commit.put("id", "commit_" + System.currentTimeMillis() + "_" + i);
            commit.put("message", "Sample commit " + (i + 1) + " for " + repository.getName());
            commit.put("author", "Developer " + (i % 3 + 1));
            commit.put("timestamp", new Date(System.currentTimeMillis() - (i * 86400000L)).toString());
            commit.put("branch", repository.getDefaultBranch() != null ? repository.getDefaultBranch() : "main");
            commit.put("analysisStatus", i == 0 ? "COMPLETED" : "PENDING");
            commit.put("issueCount", i * 2);
            
            commits.add(commit);
        }
        
        System.out.println("📝 Generated " + commits.size() + " mock commits for repository " + repository.getId());
        return commits;
    }
    
    @Transactional
    private void updateRepositoryCommitStats(RepositoryEntity repository, List<Object> commits) {
        try {
            System.out.println("🔧 Starting updateRepositoryCommitStats for repository " + repository.getId());
            if (!commits.isEmpty()) {
                // Update commit count
                repository.setCommitCount(commits.size());
                System.out.println("🔧 Set commit count to: " + commits.size());
                
                // Update last commit date - get the most recent commit timestamp
                Map<String, Object> latestCommit = (Map<String, Object>) commits.get(0);
                String timestampStr = (String) latestCommit.get("timestamp");
                if (timestampStr != null) {
                    try {
                        // Try parsing the timestamp string - if it's already a Date.toString() format, 
                        // we can use Date constructor, otherwise use current time
                        Date commitDate;
                        if (timestampStr.contains("UTC") || timestampStr.contains("GMT")) {
                            // This is likely from Date.toString(), parse it directly
                            commitDate = new Date(timestampStr);
                        } else {
                            // For other formats, just use current time
                            commitDate = new Date();
                        }
                        repository.setLastCommitDate(commitDate.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime());
                        System.out.println("✅ Parsed commit timestamp: " + timestampStr + " -> " + repository.getLastCommitDate());
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not parse commit timestamp: " + timestampStr + " Error: " + e.getMessage());
                        repository.setLastCommitDate(java.time.LocalDateTime.now());
                    }
                } else {
                    repository.setLastCommitDate(java.time.LocalDateTime.now());
                    System.out.println("🔧 Set last commit date to current time");
                }
                
                // Save the updated repository with flush to force immediate persistence
                System.out.println("🔧 Saving repository to database...");
                RepositoryEntity savedRepo = repositoryRepository.saveAndFlush(repository);
                System.out.println("✅ Updated repository " + savedRepo.getId() + " with " + commits.size() + " commits - commitCount now: " + savedRepo.getCommitCount());
            } else {
                System.out.println("⚠️ No commits to update stats with");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to update repository commit stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractFullNameFromUrl(String url) {
        try {
            // Extract owner/repo from GitHub/GitLab URL
            String[] parts = url.split("/");
            if (parts.length >= 2) {
                String owner = parts[parts.length - 2];
                String repo = parts[parts.length - 1].replace(".git", "");
                return owner + "/" + repo;
            }
        } catch (Exception e) {
            // If extraction fails, return the repository name
        }
        return null;
    }
    
    private String extractExternalIdFromUrl(String url) {
        try {
            // Extract owner/repo from GitHub/GitLab URL for external ID
            String[] parts = url.replace(".git", "").split("/");
            if (parts.length >= 2) {
                String owner = parts[parts.length - 2];
                String repo = parts[parts.length - 1];
                return owner + "/" + repo;
            }
        } catch (Exception e) {
            // If extraction fails, return a UUID
        }
        return java.util.UUID.randomUUID().toString();
    }

    private void sendRepositoryNotification(String eventType, RepositoryDto repository) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", eventType);
            notification.put("repositoryId", repository.getId());
            notification.put("repositoryName", repository.getName());
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("data", repository);
            
            // Send to Kafka
            kafkaTemplate.send("repository-events", notification);
            System.out.println("Repository event sent to Kafka: " + notification);
        } catch (Exception e) {
            System.err.println("Failed to send repository notification: " + e.getMessage());
        }
    }

    private RepositoryDto convertToDto(RepositoryEntity entity) {
        RepositoryDto dto = new RepositoryDto(entity.getName(), entity.getPlatform(), entity.getUrl());
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setDescription(entity.getDescription());
        dto.setLanguage(entity.getLanguage());
        dto.setDefaultBranch(entity.getDefaultBranch());
        dto.setPrivate(entity.isPrivate());
        dto.setCommitCount(entity.getCommitCount());
        dto.setBranchCount(entity.getBranchCount());
        dto.setAnalysisCount(entity.getAnalysisCount());
        dto.setIssueCount(entity.getIssueCount());
        dto.setSecurityStatus(entity.getSecurityStatus());
        dto.setWebhookConfigured(entity.isWebhookConfigured());
        dto.setLastCommitDate(entity.getLastCommitDate());
        dto.setLastAnalysisDate(entity.getLastAnalysisDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private RepositoryEntity convertToEntity(RepositoryDto dto) {
        RepositoryEntity entity = new RepositoryEntity(dto.getName(), dto.getPlatform(), dto.getUrl());
        if (dto.getId() != null) entity.setId(dto.getId());
        entity.setFullName(dto.getFullName());
        entity.setDescription(dto.getDescription());
        entity.setLanguage(dto.getLanguage());
        entity.setDefaultBranch(dto.getDefaultBranch());
        entity.setPrivate(dto.isPrivate());
        entity.setCommitCount(dto.getCommitCount());
        entity.setBranchCount(dto.getBranchCount());
        entity.setAnalysisCount(dto.getAnalysisCount());
        entity.setIssueCount(dto.getIssueCount());
        entity.setSecurityStatus(dto.getSecurityStatus());
        entity.setWebhookConfigured(dto.isWebhookConfigured());
        entity.setLastCommitDate(dto.getLastCommitDate());
        entity.setLastAnalysisDate(dto.getLastAnalysisDate());
        // Set clone URL to same as URL if not provided
        entity.setCloneUrl(dto.getUrl());
        // Set external ID
        entity.setExternalId(extractExternalIdFromUrl(dto.getUrl()));
        if (dto.getCreatedAt() != null) entity.setCreatedAt(dto.getCreatedAt());
        if (dto.getUpdatedAt() != null) entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }
}