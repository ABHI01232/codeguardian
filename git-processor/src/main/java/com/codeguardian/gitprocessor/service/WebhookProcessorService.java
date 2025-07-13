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

        if ("push".equals(eventType)) {
            processPushEvent(payload);
        } else if ("pull_request".equals(eventType)) {
            processPullRequestEvent(payload);
        }
    }

    @Transactional
    public void processGitLabWebhook(String eventType, GitLabWebhookPayload payload) {
        log.info("Processing GitLab webhook: {}", eventType);

        if ("push".equals(payload.getObject_kind())) {
            processPushEvent(payload);
        } else if ("merge_request".equals(payload.getObject_kind())) {
            processMergeRequestEvent(payload);
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
        if (payload.getCommits() != null) {
            for (GitHubWebhookPayload.Commit commit : payload.getCommits()) {
                processCommit(commit, repository);
            }
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
        if (payload.getCommits() != null) {
            for (GitLabWebhookPayload.Commit commit : payload.getCommits()) {
                processCommit(commit, repository);
            }
        }
    }

//    private void processCommit(GitHubWebhookPayload.Commit gitCommit, RepositoryEntity repository) {
//        CommitEntity commit = commitRepository
//                .findByCommitId(gitCommit.getId())
//                .orElseGet(() -> {};
//}

    private void processCommit(GitHubWebhookPayload.Commit gitCommit, RepositoryEntity repository) {
        CommitEntity commit = commitRepository.findByCommitId(gitCommit.getId()).orElseThrow(() -> new RuntimeException("Commit not found"));
    }
}
