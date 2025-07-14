package com.codeguardian.gitprocessor.integration;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import com.codeguardian.gitprocessor.model.GitHubWebhookPayload;
import com.codeguardian.gitprocessor.repository.CommitRepository;
import com.codeguardian.gitprocessor.repository.RepositoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@EmbeddedKafka(partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "webhook.github.secret=test-secret",
        "webhook.gitlab.token=test-token",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
public class WebhookIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private CommitRepository commitRepository;

    @Test
    @Transactional
    public void testGitHubWebhookProcessing() throws Exception {
        // Create test payload
        GitHubWebhookPayload payload = createTestGitHubPayload();
        String jsonPayload = objectMapper.writeValueAsString(payload);

        // Send webhook request
        mockMvc.perform(post("/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .header("X-GitHub-Delivery", "test-delivery-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        // Verify repository was created
        Optional<RepositoryEntity> repository = repositoryRepository.findByExternalId("12345");
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isEqualTo("test-repo");
        assertThat(repository.get().getPlatform()).isEqualTo("GITHUB");

        // Verify commits were created
        List<CommitEntity> commits = commitRepository.findAll();
        assertThat(commits).hasSize(1);
        assertThat(commits.get(0).getCommitId()).isEqualTo("abc123");
        assertThat(commits.get(0).getAnalysisStatus()).isEqualTo("PENDING");
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/webhooks/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("git-processor"));
    }

    @Test
    public void testWebhookConfigEndpoint() throws Exception {
        mockMvc.perform(get("/webhooks/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedPlatforms").isArray())
                .andExpect(jsonPath("$.endpoints").exists());
    }

    @Test
    public void testRepositoryEndpoints() throws Exception {
        // Create a test repository
        RepositoryEntity repository = new RepositoryEntity();
        repository.setExternalId("test-123");
        repository.setName("test-repo");
        repository.setFullName("test-org/test-repo");
        repository.setUrl("https://github.com/test-org/test-repo");
        repository.setCloneUrl("https://github.com/test-org/test-repo.git");
        repository.setPlatform("GITHUB");
        repository.setDefaultBranch("main");
        repository = repositoryRepository.save(repository);

        // Test get all repositories
        mockMvc.perform(get("/api/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Test get specific repository
        mockMvc.perform(get("/api/repositories/" + repository.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-repo"));

        // Test get repositories by platform
        mockMvc.perform(get("/api/repositories/platform/GITHUB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private GitHubWebhookPayload createTestGitHubPayload() {
        GitHubWebhookPayload payload = new GitHubWebhookPayload();
        payload.setRef("refs/heads/main");
        payload.setBefore("oldsha");
        payload.setAfter("newsha");

        // Repository
        GitHubWebhookPayload.Repository repository = new GitHubWebhookPayload.Repository();
        repository.setId(12345L);
        repository.setName("test-repo");
        repository.setFull_name("test-org/test-repo");
        repository.setHtml_url("https://github.com/test-org/test-repo");
        repository.setClone_url("https://github.com/test-org/test-repo.git");
        repository.setDefault_branch("main");
        payload.setRepository(repository);

        // Pusher
        GitHubWebhookPayload.Pusher pusher = new GitHubWebhookPayload.Pusher();
        pusher.setName("test-user");
        pusher.setEmail("test@example.com");
        payload.setPusher(pusher);

        // Commit
        GitHubWebhookPayload.Commit commit = new GitHubWebhookPayload.Commit();
        commit.setId("abc123");
        commit.setMessage("Test commit");
        commit.setTimestamp("2024-01-01T12:00:00Z");
        commit.setUrl("https://github.com/test-org/test-repo/commit/abc123");

        GitHubWebhookPayload.Author author = new GitHubWebhookPayload.Author();
        author.setName("test-author");
        author.setEmail("author@example.com");
        author.setUsername("test-author");
        commit.setAuthor(author);

        commit.setAdded(Arrays.asList("new-file.java"));
        commit.setModified(Arrays.asList("existing-file.java"));
        commit.setRemoved(Arrays.asList("old-file.java"));

        payload.setCommits(Arrays.asList(commit));

        return payload;
    }
}
