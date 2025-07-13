package com.codeguardian.gitprocessor.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "commits")
@Data
public class CommitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String commitId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String authorName;
    private String authorEmail;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "repository_id")
    private RepositoryEntity repository;

    @Column(columnDefinition = "TEXT")
    private String filesAdded;

    @Column(columnDefinition = "TEXT")
    private String filesModified;

    @Column(columnDefinition = "TEXT")
    private String filesRemoved;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String analysisStatus; // PENDING, IN_PROGRESS, COMPLETED, FAILED
}
