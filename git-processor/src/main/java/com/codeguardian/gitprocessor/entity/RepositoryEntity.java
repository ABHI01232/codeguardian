package com.codeguardian.gitprocessor.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
@Data
public class RepositoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String cloneUrl;

    @Column(nullable = false)
    private String platform; // GITHUB or GITLAB

    private String defaultBranch;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
