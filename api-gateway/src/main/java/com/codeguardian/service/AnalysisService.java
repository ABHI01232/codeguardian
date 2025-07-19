package com.codeguardian.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private AnalysisProcessingService analysisProcessingService;

    private final Map<String, Object> analyses = new ConcurrentHashMap<>();

    public AnalysisService() {
        // No mock data initialization - completely clean start
    }
    
    @jakarta.annotation.PostConstruct
    public void initializeCleanState() {
        // Clear any existing in-memory analyses and ensure clean start
        analyses.clear();
        System.out.println("✅ AnalysisService initialized - completely clean, no mock data");
    }

    public List<Object> getAllAnalyses() {
        return new ArrayList<>(analyses.values());
    }

    public Optional<Object> getAnalysisById(String id) {
        // First try to get from database
        Map<String, Object> analysis = analysisProcessingService.getAnalysisById(id);
        if (analysis != null) {
            return Optional.of(analysis);
        }
        // Fallback to in-memory storage
        return Optional.ofNullable(analyses.get(id));
    }

    public List<Object> getRecentAnalyses(int limit) {
        // Get all completed analyses from all repositories (for dashboard)
        List<Object> allAnalyses = new ArrayList<>();
        
        // Add in-memory analyses for backward compatibility (should be empty now)
        allAnalyses.addAll(analyses.values().stream()
                .sorted((a, b) -> {
                    String timeA = (String) ((Map<String, Object>) a).get("timestamp");
                    String timeB = (String) ((Map<String, Object>) b).get("timestamp");
                    return timeB.compareTo(timeA);
                })
                .limit(limit)
                .collect(Collectors.toList()));
                
        return allAnalyses;
    }

    public List<Object> getAnalysesByRepository(Long repositoryId) {
        // Get analyses from database first
        List<Map<String, Object>> dbAnalyses = analysisProcessingService.getAnalysesByRepository(repositoryId);
        List<Object> result = new ArrayList<>(dbAnalyses);
        
        // Add in-memory analyses for backward compatibility (should be empty now)
        result.addAll(analyses.values().stream()
                .filter(analysis -> {
                    Map<String, Object> analysisMap = (Map<String, Object>) analysis;
                    return repositoryId.equals(analysisMap.get("repositoryId"));
                })
                .collect(Collectors.toList()));
                
        return result;
    }

    public List<Object> getAnalysesByStatus(String status) {
        return analyses.values().stream()
                .filter(analysis -> {
                    Map<String, Object> analysisMap = (Map<String, Object>) analysis;
                    return status.equalsIgnoreCase((String) analysisMap.get("status"));
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> rerunAnalysis(String id) {
        Map<String, Object> analysis = (Map<String, Object>) analyses.get(id);
        if (analysis == null) {
            throw new RuntimeException("Analysis not found with id: " + id);
        }

        // Create new analysis with new ID
        String newId = "analysis_" + System.currentTimeMillis();
        Map<String, Object> newAnalysis = new HashMap<>(analysis);
        newAnalysis.put("id", newId);
        newAnalysis.put("status", "RUNNING");
        newAnalysis.put("timestamp", LocalDateTime.now().toString());
        newAnalysis.put("findings", Map.of("critical", 0, "high", 0, "medium", 0, "low", 0));
        
        analyses.put(newId, newAnalysis);
        
        // Send to Kafka for processing
        kafkaTemplate.send("analysis-requests", newAnalysis);
        System.out.println("Analysis request sent to Kafka: " + newAnalysis);
        
        return Map.of("analysisId", newId, "status", "started");
    }

    public void deleteAnalysis(String id) {
        Object analysis = analyses.remove(id);
        if (analysis == null) {
            throw new RuntimeException("Analysis not found with id: " + id);
        }
    }

    public Map<String, Object> getAnalysisStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalAnalyses = analyses.size();
        long completedAnalyses = analyses.values().stream()
                .filter(analysis -> "COMPLETED".equals(((Map<String, Object>) analysis).get("status")))
                .count();
        long runningAnalyses = analyses.values().stream()
                .filter(analysis -> "RUNNING".equals(((Map<String, Object>) analysis).get("status")))
                .count();
        long failedAnalyses = analyses.values().stream()
                .filter(analysis -> "FAILED".equals(((Map<String, Object>) analysis).get("status")))
                .count();
        
        // Calculate total findings
        int totalCritical = 0, totalHigh = 0, totalMedium = 0, totalLow = 0;
        for (Object analysis : analyses.values()) {
            Map<String, Object> findings = (Map<String, Object>) ((Map<String, Object>) analysis).get("findings");
            if (findings != null) {
                totalCritical += (Integer) findings.getOrDefault("critical", 0);
                totalHigh += (Integer) findings.getOrDefault("high", 0);
                totalMedium += (Integer) findings.getOrDefault("medium", 0);
                totalLow += (Integer) findings.getOrDefault("low", 0);
            }
        }
        
        stats.put("totalAnalyses", totalAnalyses);
        stats.put("completedAnalyses", completedAnalyses);
        stats.put("runningAnalyses", runningAnalyses);
        stats.put("failedAnalyses", failedAnalyses);
        stats.put("totalFindings", Map.of(
                "critical", totalCritical,
                "high", totalHigh,
                "medium", totalMedium,
                "low", totalLow
        ));
        
        return stats;
    }

    public List<Object> getAnalysisFindings(String id) {
        Map<String, Object> analysis = (Map<String, Object>) analyses.get(id);
        if (analysis == null) {
            return new ArrayList<>();
        }
        
        List<Object> details = (List<Object>) analysis.get("details");
        return details != null ? details : new ArrayList<>();
    }

    public List<Object> getFindingsBySeverity(String severity) {
        List<Object> findings = new ArrayList<>();
        
        for (Object analysis : analyses.values()) {
            Map<String, Object> analysisMap = (Map<String, Object>) analysis;
            List<Object> details = (List<Object>) analysisMap.get("details");
            
            if (details != null) {
                findings.addAll(details.stream()
                        .filter(finding -> {
                            Map<String, Object> findingMap = (Map<String, Object>) finding;
                            return severity.equalsIgnoreCase((String) findingMap.get("severity"));
                        })
                        .collect(Collectors.toList()));
            }
        }
        
        return findings;
    }
    
    /**
     * Store a completed analysis from the processing service
     */
    public void storeCompletedAnalysis(String analysisId, Map<String, Object> analysisData) {
        analyses.put(analysisId, analysisData);
        System.out.println("✅ Stored completed analysis: " + analysisId);
    }
}