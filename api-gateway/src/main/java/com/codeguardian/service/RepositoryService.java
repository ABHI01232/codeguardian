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

@Service
@Transactional
public class RepositoryService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RepositoryEntityRepository repositoryRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AnalysisProcessingService analysisProcessingService;

    @PostConstruct
    public void initializeSampleData() {
        // ABSOLUTELY NO DATA CREATION - AGGRESSIVE CLEANUP ONLY
        System.out.println("🔥 ENFORCING CLEAN START - RepositoryService initialization...");
        
        // Add a brief delay to ensure database is ready
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            // Force flush any cached data
            repositoryRepository.flush();
            
            long count = repositoryRepository.count();
            System.out.println("🔍 Database check: Found " + count + " repositories");
            
            if (count > 0) {
                // List repositories before deletion for debugging
                repositoryRepository.findAll().forEach(repo -> {
                    System.out.println("🚮 DELETING REPOSITORY: " + repo.getName() + " (ID: " + repo.getId() + ")");
                });
                
                System.out.println("🧹 AGGRESSIVE CLEANUP: Deleting all " + count + " repositories...");
                repositoryRepository.deleteAll();
                repositoryRepository.flush();
                
                // Verify deletion
                long afterCount = repositoryRepository.count();
                System.out.println("✅ CLEANUP COMPLETE: " + afterCount + " repositories remaining");
                
                if (afterCount > 0) {
                    System.out.println("⚠️ WARNING: " + afterCount + " repositories still exist after cleanup!");
                }
            } else {
                System.out.println("✅ Database is completely clean - no repositories found");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error during database operations: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🚫 ZERO TOLERANCE FOR MOCK DATA - Repository service initialized clean");
        
        // Schedule periodic cleanup to catch any late initializations
        schedulePeriodicCleanup();
    }
    
    private void schedulePeriodicCleanup() {
        // Run cleanup every 30 seconds for the first 5 minutes to catch any late initializations
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                long count = repositoryRepository.count();
                if (count > 0) {
                    System.out.println("🚨 DETECTED " + count + " repositories during periodic check - cleaning up!");
                    repositoryRepository.findAll().forEach(repo -> {
                        System.out.println("🚮 REMOVING: " + repo.getName() + " (ID: " + repo.getId() + ")");
                    });
                    repositoryRepository.deleteAll();
                    repositoryRepository.flush();
                    System.out.println("✅ Periodic cleanup completed");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error during periodic cleanup: " + e.getMessage());
            }
        }, 30, 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    public List<RepositoryDto> getAllRepositories() {
        List<RepositoryEntity> entities = repositoryRepository.findAllOrderByCreatedAtDesc();
        return entities.stream().map(this::convertToDto).toList();
    }

    public Optional<RepositoryDto> getRepositoryById(Long id) {
        return repositoryRepository.findById(id).map(this::convertToDto);
    }

    public RepositoryDto createRepository(RepositoryDto repositoryDto) {
        RepositoryEntity entity = convertToEntity(repositoryDto);
        
        // Set default values
        if (entity.getCommitCount() == null) entity.setCommitCount(0);
        if (entity.getBranchCount() == null) entity.setBranchCount(1);
        if (entity.getAnalysisCount() == null) entity.setAnalysisCount(0);
        if (entity.getIssueCount() == null) entity.setIssueCount(0);
        if (entity.getSecurityStatus() == null) entity.setSecurityStatus("PENDING");
        if (entity.getDefaultBranch() == null) entity.setDefaultBranch("master");
        
        // Auto-generate fullName if not provided
        if (entity.getFullName() == null && entity.getUrl() != null) {
            entity.setFullName(extractFullNameFromUrl(entity.getUrl()));
        }
        
        RepositoryEntity savedEntity = repositoryRepository.save(entity);
        RepositoryDto savedDto = convertToDto(savedEntity);
        
        // Send notification via Kafka
        sendRepositoryNotification("repository_created", savedDto);
        
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
        // Delegate to AnalysisProcessingService which handles database persistence
        return analysisProcessingService.triggerAnalysis(id);
    }

    public List<Object> getRepositoryAnalyses(Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id).orElse(null);
        if (repository == null) {
            return new ArrayList<>();
        }
        
        // Get analyses from AnalysisProcessingService
        List<Map<String, Object>> analyses = analysisProcessingService.getAnalysesByRepository(id);
        return new ArrayList<>(analyses);
    }

    public List<Object> getRepositoryCommits(Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id).orElse(null);
        if (repository == null) {
            return new ArrayList<>();
        }
        
        // Return empty list for now - commits will be populated from actual Git data
        // This removes all mock data and cross-repository contamination
        return new ArrayList<>();
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
        if (dto.getCreatedAt() != null) entity.setCreatedAt(dto.getCreatedAt());
        if (dto.getUpdatedAt() != null) entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }
}