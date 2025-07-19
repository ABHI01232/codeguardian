package com.codeguardian.controller;

import com.codeguardian.dto.RepositoryDto;
import com.codeguardian.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/repositories")
@CrossOrigin(origins = "http://localhost:3000")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping
    public ResponseEntity<List<RepositoryDto>> getAllRepositories() {
        List<RepositoryDto> repositories = repositoryService.getAllRepositories();
        System.out.println("📋 API Request: getAllRepositories() returning " + repositories.size() + " repositories");
        return ResponseEntity.ok(repositories);
    }
    
    @PostMapping("/debug/clear-all")
    public ResponseEntity<Map<String, Object>> debugClearAll() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<RepositoryDto> before = repositoryService.getAllRepositories();
            result.put("beforeCount", before.size());
            
            // Force clear all repositories
            before.forEach(repo -> repositoryService.deleteRepository(repo.getId()));
            
            List<RepositoryDto> after = repositoryService.getAllRepositories();
            result.put("afterCount", after.size());
            result.put("success", true);
            
            System.out.println("🧹 DEBUG CLEAR: Removed " + before.size() + " repositories, " + after.size() + " remaining");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepositoryDto> getRepositoryById(@PathVariable Long id) {
        Optional<RepositoryDto> repository = repositoryService.getRepositoryById(id);
        return repository.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RepositoryDto> createRepository(@RequestBody RepositoryDto repositoryDto) {
        try {
            RepositoryDto createdRepository = repositoryService.createRepository(repositoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRepository);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepositoryDto> updateRepository(@PathVariable Long id, @RequestBody RepositoryDto repositoryDto) {
        try {
            RepositoryDto updatedRepository = repositoryService.updateRepository(id, repositoryDto);
            return ResponseEntity.ok(updatedRepository);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(@PathVariable Long id) {
        try {
            repositoryService.deleteRepository(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeRepository(@PathVariable Long id) {
        try {
            String analysisId = repositoryService.triggerAnalysis(id);
            return ResponseEntity.ok("{\"analysisId\": \"" + analysisId + "\", \"status\": \"started\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to start analysis\"}");
        }
    }

    @GetMapping("/{id}/analyses")
    public ResponseEntity<List<Object>> getRepositoryAnalyses(@PathVariable Long id) {
        List<Object> analyses = repositoryService.getRepositoryAnalyses(id);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}/commits")
    public ResponseEntity<List<Object>> getRepositoryCommits(@PathVariable Long id) {
        List<Object> commits = repositoryService.getRepositoryCommits(id);
        return ResponseEntity.ok(commits);
    }
}