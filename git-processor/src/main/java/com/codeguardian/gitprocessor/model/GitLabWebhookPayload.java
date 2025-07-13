package com.codeguardian.gitprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabWebhookPayload {
    private String object_kind;
    private String ref;
    private String checkout_sha;
    private String before;
    private String after;
    private Project project;
    private List<Commit> commits;
    private MergeRequest merge_request;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private Long id;
        private String name;
        private String web_url;
        private String git_http_url;
        private String default_branch;
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
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MergeRequest {
        private Long id;
        private String state;
        private String title;
        private String description;
        private String url;
    }
}
