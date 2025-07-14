package com.codeguardian.gitprocessor.controller;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import com.codeguardian.gitprocessor.repository.CommitRepository;
import com.codeguardian.gitprocessor.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepositoryController {
    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;

    @GetMapping
    public Page<RepositoryEntity> getAllRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repositoryRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepositoryEntity> getRepository(@PathVariable Long id) {
        Optional<RepositoryEntity> repository = repositoryRepository.findById(id);
        return repository.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/commits")
    public List<CommitEntity> getRepositoryCommits(@PathVariable Long id) {
        RepositoryEntity repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        return commitRepository.findByRepository(repository);
    }

    @GetMapping("/platform/{platform}")
    public List<RepositoryEntity> getRepositoriesByPlatform(@PathVariable String platform) {
        return repositoryRepository.findByPlatform(platform.toUpperCase());
    }
}
