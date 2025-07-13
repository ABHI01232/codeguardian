package com.codeguardian.gitprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWebhookPayload {
    private String ref;
    private String before;
    private String after;
    private Repository repository;
    private Pusher pusher;
    private List<Commit> commits;
    private String action;
    private PullRequest pull_request;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private Long id;
        private String name;
        private String full_name;
        private String html_url;
        private String clone_url;
        private String default_branch;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pusher {
        private String name;
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String id;
        private String message;
        private String timestamp;
        private String url;
        private Author author;
        private List<String> added;
        private List<String> removed;
        private List<String> modified;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
        private String username;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequest {
        private Long id;
        private String state;
        private String title;
        private String body;
        private String html_url;
        private String diff_url;
    }
}
