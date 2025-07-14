package com.codeguardian.gitprocessor.service;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import com.codeguardian.gitprocessor.model.GitHubWebhookPayload;
import com.codeguardian.gitprocessor.model.GitLabWebhookPayload;
import com.codeguardian.gitprocessor.repository.CommitRepository;
import com.codeguardian.gitprocessor.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessorService {

    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void processGitHubWebhook(String eventType, GitHubWebhookPayload payload) {
        log.info("Processing GitHub webhook: {}", eventType);

        try {
            if ("push".equals(eventType)) {
                processPushEvent(payload);
            } else if ("pull_request".equals(eventType)) {
                processPullRequestEvent(payload);
            } else {
                log.info("Unsupported GitHub event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing GitHub webhook", e);
            throw e;
        }
    }

    @Transactional
    public void processGitLabWebhook(String eventType, GitLabWebhookPayload payload) {
        log.info("Processing GitLab webhook: {}", eventType);

        try {
            if ("push".equals(payload.getObject_kind())) {
                processPushEvent(payload);
            } else if ("merge_request".equals(payload.getObject_kind())) {
                processMergeRequestEvent(payload);
            } else {
                log.info("Unsupported GitLab event type: {}", payload.getObject_kind());
            }
        } catch (Exception e) {
            log.error("Error processing GitLab webhook", e);
            throw e;
        }
    }

    private void processPushEvent(GitHubWebhookPayload payload) {
        // Save or update repository
        RepositoryEntity repository = repositoryRepository
                .findByExternalId(String.valueOf(payload.getRepository().getId()))
                .orElseGet(() -> {
                    RepositoryEntity newRepo = new RepositoryEntity();
                    newRepo.setExternalId(String.valueOf(payload.getRepository().getId()));
                    newRepo.setName(payload.getRepository().getName());
                    newRepo.setFullName(payload.getRepository().getFull_name());
                    newRepo.setUrl(payload.getRepository().getHtml_url());
                    newRepo.setCloneUrl(payload.getRepository().getClone_url());
                    newRepo.setPlatform("GITHUB");
                    newRepo.setDefaultBranch(payload.getRepository().getDefault_branch());
                    return repositoryRepository.save(newRepo);
                });

        // Process commits
        if (payload.getCommits() != null && !payload.getCommits().isEmpty()) {
            for (GitHubWebhookPayload.Commit commit : payload.getCommits()) {
                processCommit(commit, repository);
            }
            log.info("Processed {} commits for repository: {}", payload.getCommits().size(), repository.getFullName());
        } else {
            log.info("No commits to process for repository: {}", repository.getFullName());
        }
    }

    private void processPushEvent(GitLabWebhookPayload payload) {
        // Save or update repository
        RepositoryEntity repository = repositoryRepository
                .findByExternalId(String.valueOf(payload.getProject().getId()))
                .orElseGet(() -> {
                    RepositoryEntity newRepo = new RepositoryEntity();
                    newRepo.setExternalId(String.valueOf(payload.getProject().getId()));
                    newRepo.setName(payload.getProject().getName());
                    newRepo.setFullName(payload.getProject().getName());
                    newRepo.setUrl(payload.getProject().getWeb_url());
                    newRepo.setCloneUrl(payload.getProject().getGit_http_url());
                    newRepo.setPlatform("GITLAB");
                    newRepo.setDefaultBranch(payload.getProject().getDefault_branch());
                    return repositoryRepository.save(newRepo);
                });

        // Process commits
        if (payload.getCommits() != null && !payload.getCommits().isEmpty()) {
            for (GitLabWebhookPayload.Commit commit : payload.getCommits()) {
                processCommit(commit, repository);
            }
            log.info("Processed {} commits for repository: {}", payload.getCommits().size(), repository.getFullName());
        } else {
            log.info("No commits to process for repository: {}", repository.getFullName());
        }
    }

    private void processCommit(GitHubWebhookPayload.Commit gitCommit, RepositoryEntity repository) {
        CommitEntity commit = commitRepository
                .findByCommitId(gitCommit.getId())
                .orElseGet(() -> {
                    CommitEntity newCommit = new CommitEntity();
                    newCommit.setCommitId(gitCommit.getId());
                    newCommit.setMessage(gitCommit.getMessage());
                    newCommit.setAuthorName(gitCommit.getAuthor().getName());
                    newCommit.setAuthorEmail(gitCommit.getAuthor().getEmail());
                    newCommit.setRepository(repository);
                    newCommit.setAnalysisStatus("PENDING");

                    // Parse timestamp
                    if (gitCommit.getTimestamp() != null) {
                        try {
                            newCommit.setTimestamp(LocalDateTime.parse(gitCommit.getTimestamp(),
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        } catch (Exception e) {
                            log.warn("Failed to parse timestamp: {}", gitCommit.getTimestamp());
                            newCommit.setTimestamp(LocalDateTime.now());
                        }
                    } else {
                        newCommit.setTimestamp(LocalDateTime.now());
                    }

                    // Store file changes
                    if (gitCommit.getAdded() != null && !gitCommit.getAdded().isEmpty()) {
                        newCommit.setFilesAdded(String.join(",", gitCommit.getAdded()));
                    }
                    if (gitCommit.getModified() != null && !gitCommit.getModified().isEmpty()) {
                        newCommit.setFilesModified(String.join(",", gitCommit.getModified()));
                    }
                    if (gitCommit.getRemoved() != null && !gitCommit.getRemoved().isEmpty()) {
                        newCommit.setFilesRemoved(String.join(",", gitCommit.getRemoved()));
                    }

                    return commitRepository.save(newCommit);
                });

        // Send analysis request to Kafka
        sendAnalysisRequest(commit);
        log.debug("Processed commit: {} for repository: {}", commit.getCommitId(), repository.getFullName());
    }

    private void processCommit(GitLabWebhookPayload.Commit gitCommit, RepositoryEntity repository) {
        CommitEntity commit = commitRepository
                .findByCommitId(gitCommit.getId())
                .orElseGet(() -> {
                    CommitEntity newCommit = new CommitEntity();
                    newCommit.setCommitId(gitCommit.getId());
                    newCommit.setMessage(gitCommit.getMessage());
                    newCommit.setAuthorName(gitCommit.getAuthor().getName());
                    newCommit.setAuthorEmail(gitCommit.getAuthor().getEmail());
                    newCommit.setRepository(repository);
                    newCommit.setAnalysisStatus("PENDING");

                    // Parse timestamp
                    if (gitCommit.getTimestamp() != null) {
                        try {
                            newCommit.setTimestamp(LocalDateTime.parse(gitCommit.getTimestamp(),
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        } catch (Exception e) {
                            log.warn("Failed to parse timestamp: {}", gitCommit.getTimestamp());
                            newCommit.setTimestamp(LocalDateTime.now());
                        }
                    } else {
                        newCommit.setTimestamp(LocalDateTime.now());
                    }

                    // Store file changes
                    if (gitCommit.getAdded() != null && !gitCommit.getAdded().isEmpty()) {
                        newCommit.setFilesAdded(String.join(",", gitCommit.getAdded()));
                    }
                    if (gitCommit.getModified() != null && !gitCommit.getModified().isEmpty()) {
                        newCommit.setFilesModified(String.join(",", gitCommit.getModified()));
                    }
                    if (gitCommit.getRemoved() != null && !gitCommit.getRemoved().isEmpty()) {
                        newCommit.setFilesRemoved(String.join(",", gitCommit.getRemoved()));
                    }

                    return commitRepository.save(newCommit);
                });

        // Send analysis request to Kafka
        sendAnalysisRequest(commit);
        log.debug("Processed commit: {} for repository: {}", commit.getCommitId(), repository.getFullName());
    }

    private void processPullRequestEvent(GitHubWebhookPayload payload) {
        if (payload.getPull_request() != null) {
            log.info("Processing pull request: {} - {} (action: {})",
                    payload.getPull_request().getId(),
                    payload.getPull_request().getTitle(),
                    payload.getAction());

            // Create a message for pull request analysis
            Map<String, Object> pullRequestMessage = new HashMap<>();
            pullRequestMessage.put("type", "PULL_REQUEST");
            pullRequestMessage.put("platform", "GITHUB");
            pullRequestMessage.put("prId", payload.getPull_request().getId());
            pullRequestMessage.put("title", payload.getPull_request().getTitle());
            pullRequestMessage.put("body", payload.getPull_request().getBody());
            pullRequestMessage.put("state", payload.getPull_request().getState());
            pullRequestMessage.put("url", payload.getPull_request().getHtml_url());
            pullRequestMessage.put("diffUrl", payload.getPull_request().getDiff_url());
            pullRequestMessage.put("repositoryId", payload.getRepository().getId());
            pullRequestMessage.put("repositoryName", payload.getRepository().getFull_name());
            pullRequestMessage.put("action", payload.getAction());
            pullRequestMessage.put("timestamp", LocalDateTime.now().toString());

            try {
                kafkaTemplate.send("pull-request-analysis", pullRequestMessage);
                log.info("Sent pull request analysis message for PR: {}", payload.getPull_request().getId());
            } catch (Exception e) {
                log.error("Failed to send pull request analysis message", e);
            }
        }
    }

    private void processMergeRequestEvent(GitLabWebhookPayload payload) {
        if (payload.getMerge_request() != null) {
            log.info("Processing merge request: {} - {} (state: {})",
                    payload.getMerge_request().getId(),
                    payload.getMerge_request().getTitle(),
                    payload.getMerge_request().getState());

            // Create a message for merge request analysis
            Map<String, Object> mergeRequestMessage = new HashMap<>();
            mergeRequestMessage.put("type", "MERGE_REQUEST");
            mergeRequestMessage.put("platform", "GITLAB");
            mergeRequestMessage.put("mrId", payload.getMerge_request().getId());
            mergeRequestMessage.put("title", payload.getMerge_request().getTitle());
            mergeRequestMessage.put("description", payload.getMerge_request().getDescription());
            mergeRequestMessage.put("state", payload.getMerge_request().getState());
            mergeRequestMessage.put("url", payload.getMerge_request().getUrl());
            mergeRequestMessage.put("repositoryId", payload.getProject().getId());
            mergeRequestMessage.put("repositoryName", payload.getProject().getName());
            mergeRequestMessage.put("timestamp", LocalDateTime.now().toString());

            try {
                kafkaTemplate.send("merge-request-analysis", mergeRequestMessage);
                log.info("Sent merge request analysis message for MR: {}", payload.getMerge_request().getId());
            } catch (Exception e) {
                log.error("Failed to send merge request analysis message", e);
            }
        }
    }

    private void sendAnalysisRequest(CommitEntity commit) {
        try {
            Map<String, Object> analysisMessage = new HashMap<>();
            analysisMessage.put("commitId", commit.getCommitId());
            analysisMessage.put("repositoryId", commit.getRepository().getId());
            analysisMessage.put("repositoryExternalId", commit.getRepository().getExternalId());
            analysisMessage.put("repositoryName", commit.getRepository().getFullName());
            analysisMessage.put("repositoryUrl", commit.getRepository().getUrl());
            analysisMessage.put("repositoryCloneUrl", commit.getRepository().getCloneUrl());
            analysisMessage.put("platform", commit.getRepository().getPlatform());
            analysisMessage.put("message", commit.getMessage());
            analysisMessage.put("authorName", commit.getAuthorName());
            analysisMessage.put("authorEmail", commit.getAuthorEmail());
            analysisMessage.put("filesAdded", commit.getFilesAdded());
            analysisMessage.put("filesModified", commit.getFilesModified());
            analysisMessage.put("filesRemoved", commit.getFilesRemoved());
            analysisMessage.put("timestamp", commit.getTimestamp().toString());
            analysisMessage.put("analysisStatus", commit.getAnalysisStatus());

            kafkaTemplate.send("commit-analysis", commit.getCommitId(), analysisMessage);
            log.info("Sent analysis request for commit: {} from repository: {}",
                    commit.getCommitId(), commit.getRepository().getFullName());
        } catch (Exception e) {
            log.error("Failed to send analysis request for commit: {} from repository: {}",
                    commit.getCommitId(), commit.getRepository().getFullName(), e);

            // Update commit status to indicate failure
            try {
                commit.setAnalysisStatus("FAILED");
                commitRepository.save(commit);
            } catch (Exception saveException) {
                log.error("Failed to update commit status to FAILED", saveException);
            }
        }
    }

    // Helper method to update commit analysis status
    @Transactional
    public void updateCommitAnalysisStatus(String commitId, String status) {
        try {
            CommitEntity commit = commitRepository.findByCommitId(commitId)
                    .orElseThrow(() -> new RuntimeException("Commit not found: " + commitId));

            commit.setAnalysisStatus(status);
            commitRepository.save(commit);

            log.info("Updated analysis status for commit {} to {}", commitId, status);
        } catch (Exception e) {
            log.error("Failed to update analysis status for commit: {}", commitId, e);
        }
    }

    // Helper method to get repository statistics
    public Map<String, Object> getRepositoryStats(Long repositoryId) {
        try {
            RepositoryEntity repository = repositoryRepository.findById(repositoryId)
                    .orElseThrow(() -> new RuntimeException("Repository not found: " + repositoryId));

            List<CommitEntity> commits = commitRepository.findByRepository(repository);


            Map<String, Object> stats = new HashMap<>();
            stats.put("repositoryId", repository.getId());
            stats.put("repositoryName", repository.getFullName());
            stats.put("platform", repository.getPlatform());
            stats.put("totalCommits", commits.size());

            // Count commits by status
            Map<String, Long> statusCounts = commits.stream()
                    .collect(groupingBy(CommitEntity::getAnalysisStatus, counting()));
            stats.put("statusCounts", statusCounts);

            return stats;
        } catch (Exception e) {
            log.error("Failed to get repository stats for repository: {}", repositoryId, e);
            throw e;
        }
    }
}