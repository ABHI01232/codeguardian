package com.codeguardian.gitprocessor.service;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import com.codeguardian.gitprocessor.model.GitHubWebhookPayload;
import com.codeguardian.gitprocessor.repository.CommitRepository;
import com.codeguardian.gitprocessor.repository.RepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookProcessorServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private CommitRepository commitRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private WebhookProcessorService service;

    @BeforeEach
    void setUp() {
        service = new WebhookProcessorService(repositoryRepository, commitRepository, kafkaTemplate);
    }

    @Test
    void processGitHubWebhook_shouldCreateRepositoryAndCommit() {
        // Given
        GitHubWebhookPayload payload = createTestPayload();
        RepositoryEntity mockRepo = new RepositoryEntity();
        mockRepo.setId(1L);

        when(repositoryRepository.findByExternalId(anyString())).thenReturn(Optional.empty());
        when(repositoryRepository.save(any(RepositoryEntity.class))).thenReturn(mockRepo);
        when(commitRepository.findByCommitId(anyString())).thenReturn(Optional.empty());
        when(commitRepository.save(any(CommitEntity.class))).thenReturn(new CommitEntity());

        // When
        service.processGitHubWebhook("push", payload);

        // Then
        verify(repositoryRepository).save(any(RepositoryEntity.class));
        verify(commitRepository).save(any(CommitEntity.class));
        verify(kafkaTemplate).send(eq("commit-analysis"), anyString(), any());
    }

    private GitHubWebhookPayload createTestPayload() {
        GitHubWebhookPayload payload = new GitHubWebhookPayload();

        GitHubWebhookPayload.Repository repo = new GitHubWebhookPayload.Repository();
        repo.setId(123L);
        repo.setName("test");
        repo.setFull_name("test/repo");
        repo.setHtml_url("https://github.com/test/repo");
        repo.setClone_url("https://github.com/test/repo.git");
        payload.setRepository(repo);

        GitHubWebhookPayload.Commit commit = new GitHubWebhookPayload.Commit();
        commit.setId("abc123");
        commit.setMessage("Test commit");

        GitHubWebhookPayload.Author author = new GitHubWebhookPayload.Author();
        author.setName("Test Author");
        author.setEmail("test@example.com");
        commit.setAuthor(author);

        payload.setCommits(Arrays.asList(commit));

        return payload;
    }
}
