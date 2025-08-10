package com.codeguardian.controller;

import com.codeguardian.dto.RepositoryDto;
import com.codeguardian.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Repository Management", description = "APIs for managing code repositories and triggering security analysis")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping
    @Operation(summary = "Get all repositories", description = "Retrieve a list of all registered repositories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved repositories",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryDto.class)))
    })
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
    @Operation(summary = "Create repository", description = "Register a new repository for security analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Repository created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid repository data")
    })
    public ResponseEntity<?> createRepository(
            @Parameter(description = "Repository data", required = true)
            @RequestBody RepositoryDto repositoryDto) {
        try {
            RepositoryDto createdRepository = repositoryService.createRepository(repositoryDto);
            System.out.println("✅ Repository created successfully: " + createdRepository.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRepository);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create repository: " + e.getMessage()));
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
    @Operation(summary = "Delete repository", description = "Remove a repository from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Repository deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Repository not found")
    })
    public ResponseEntity<Void> deleteRepository(
            @Parameter(description = "Repository ID", required = true)
            @PathVariable Long id) {
        try {
            repositoryService.deleteRepository(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger security analysis", description = "Start security analysis for the specified repository")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analysis started successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to start analysis")
    })
    public ResponseEntity<String> analyzeRepository(
            @Parameter(description = "Repository ID", required = true)
            @PathVariable Long id) {
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
    
    @PostMapping("/{id}/update-stats")
    public ResponseEntity<Map<String, Object>> updateRepositoryStats(@PathVariable Long id) {
        try {
            repositoryService.updateRepositoryStats(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Repository stats updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}