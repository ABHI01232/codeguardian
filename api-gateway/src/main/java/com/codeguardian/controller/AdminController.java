package com.codeguardian.controller;

import com.codeguardian.repository.AnalysisEntityRepository;
import com.codeguardian.repository.RepositoryEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private RepositoryEntityRepository repositoryRepository;

    @Autowired
    private AnalysisEntityRepository analysisRepository;

    @PostMapping("/clear-database")
    public ResponseEntity<Map<String, Object>> clearDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long repoCount = repositoryRepository.count();
            long analysisCount = analysisRepository.count();
            
            // Clear all data
            analysisRepository.deleteAll();
            repositoryRepository.deleteAll();
            
            result.put("success", true);
            result.put("message", "Database cleared successfully");
            result.put("deletedRepositories", repoCount);
            result.put("deletedAnalyses", analysisCount);
            
            System.out.println("🧹 Database manually cleared - " + repoCount + " repositories and " + analysisCount + " analyses removed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to clear database: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/database-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            long repoCount = repositoryRepository.count();
            long analysisCount = analysisRepository.count();
            
            status.put("repositories", repoCount);
            status.put("analyses", analysisCount);
            status.put("isEmpty", repoCount == 0 && analysisCount == 0);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(status);
        }
    }
}