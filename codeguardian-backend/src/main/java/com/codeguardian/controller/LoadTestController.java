package com.codeguardian.controller;

import com.codeguardian.service.RepositoryService;
import com.codeguardian.service.KafkaMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/load-test")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Load Testing", description = "APIs for testing Kafka load capacity and performance")
public class LoadTestController {

    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    private KafkaMonitoringService kafkaMonitoringService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    @PostMapping("/bulk-analysis")
    @Operation(summary = "Trigger bulk analysis", description = "Send multiple analysis requests to test Kafka load capacity")
    public ResponseEntity<Map<String, Object>> bulkAnalysis(
            @Parameter(description = "Number of analysis requests to send", required = true)
            @RequestParam int count,
            @Parameter(description = "Repository ID to analyze", required = true)
            @RequestParam Long repositoryId) {
        
        System.out.println("🚀 Starting bulk analysis load test:");
        System.out.println("   • Request Count: " + count);
        System.out.println("   • Repository ID: " + repositoryId);
        System.out.println("   • Timestamp: " + new Date());
        
        // Reset counters
        successCount.set(0);
        errorCount.set(0);
        
        long startTime = System.currentTimeMillis();
        
        // Create bulk analysis requests
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            final int requestId = i + 1;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String analysisId = repositoryService.triggerAnalysis(repositoryId);
                    successCount.incrementAndGet();
                    if (requestId % 1000 == 0) {
                        System.out.println("📊 Progress: " + requestId + "/" + count + " requests sent");
                    }
                    return analysisId;
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("❌ Error in request " + requestId + ": " + e.getMessage());
                    return "error-" + requestId;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                double throughput = (double) count / (duration / 1000.0);
                
                System.out.println("✅ Bulk analysis load test completed:");
                System.out.println("   • Total Requests: " + count);
                System.out.println("   • Successful: " + successCount.get());
                System.out.println("   • Failed: " + errorCount.get());
                System.out.println("   • Duration: " + duration + "ms");
                System.out.println("   • Throughput: " + String.format("%.2f", throughput) + " req/sec");
            });
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bulk analysis started");
        response.put("totalRequests", count);
        response.put("repositoryId", repositoryId);
        response.put("startTime", new Date(startTime));
        response.put("estimatedDuration", count < 1000 ? "< 30 seconds" : count < 10000 ? "1-5 minutes" : "5+ minutes");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-test-repositories")
    @Operation(summary = "Create test repositories", description = "Create multiple repositories for load testing")
    public ResponseEntity<Map<String, Object>> createTestRepositories(
            @Parameter(description = "Number of test repositories to create")
            @RequestParam(defaultValue = "100") int count) {
        
        System.out.println("📦 Creating " + count + " test repositories...");
        
        List<Long> repositoryIds = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            try {
                com.codeguardian.dto.RepositoryDto repoDto = new com.codeguardian.dto.RepositoryDto();
                repoDto.setName("load-test-repo-" + i);
                repoDto.setUrl("https://github.com/test/repo-" + i + ".git");
                repoDto.setDefaultBranch("main");
                repoDto.setDescription("Load test repository " + i);
                
                com.codeguardian.dto.RepositoryDto created = repositoryService.createRepository(repoDto);
                repositoryIds.add(created.getId());
                
                if (i % 50 == 0) {
                    System.out.println("📊 Created " + i + "/" + count + " repositories");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to create repository " + i + ": " + e.getMessage());
            }
        }
        
        System.out.println("✅ Created " + repositoryIds.size() + " test repositories");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test repositories created");
        response.put("createdCount", repositoryIds.size());
        response.put("repositoryIds", repositoryIds.subList(0, Math.min(10, repositoryIds.size()))); // Show first 10
        response.put("totalIds", repositoryIds);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stress-test")
    @Operation(summary = "Run comprehensive stress test", description = "Test multiple repositories with concurrent analysis requests")
    public ResponseEntity<Map<String, Object>> stressTest(
            @Parameter(description = "Number of concurrent threads")
            @RequestParam(defaultValue = "10") int threads,
            @Parameter(description = "Requests per thread")
            @RequestParam(defaultValue = "100") int requestsPerThread) {
        
        int totalRequests = threads * requestsPerThread;
        
        System.out.println("💥 Starting Kafka stress test:");
        System.out.println("   • Concurrent Threads: " + threads);
        System.out.println("   • Requests per Thread: " + requestsPerThread);
        System.out.println("   • Total Requests: " + totalRequests);
        
        long startTime = System.currentTimeMillis();
        successCount.set(0);
        errorCount.set(0);
        
        // Create a thread pool for stress testing
        ExecutorService stressTestPool = Executors.newFixedThreadPool(threads);
        
        List<CompletableFuture<Void>> threadFutures = new ArrayList<>();
        
        for (int t = 0; t < threads; t++) {
            final int threadId = t + 1;
            
            CompletableFuture<Void> threadFuture = CompletableFuture.runAsync(() -> {
                System.out.println("🔥 Thread " + threadId + " starting " + requestsPerThread + " requests");
                
                for (int r = 0; r < requestsPerThread; r++) {
                    try {
                        // Use repository ID 1 for simplicity (create it first)
                        repositoryService.triggerAnalysis(1L);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
                
                System.out.println("✅ Thread " + threadId + " completed");
            }, stressTestPool);
            
            threadFutures.add(threadFuture);
        }
        
        // Monitor progress
        CompletableFuture.allOf(threadFutures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                double throughput = (double) totalRequests / (duration / 1000.0);
                
                System.out.println("🏁 Stress test completed:");
                System.out.println("   • Total Requests: " + totalRequests);
                System.out.println("   • Successful: " + successCount.get());
                System.out.println("   • Failed: " + errorCount.get());
                System.out.println("   • Duration: " + duration + "ms");
                System.out.println("   • Throughput: " + String.format("%.2f", throughput) + " req/sec");
                System.out.println("   • Success Rate: " + String.format("%.2f", (double) successCount.get() / totalRequests * 100) + "%");
                
                stressTestPool.shutdown();
            });
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Stress test started");
        response.put("threads", threads);
        response.put("requestsPerThread", requestsPerThread);
        response.put("totalRequests", totalRequests);
        response.put("startTime", new Date(startTime));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get load test metrics", description = "View current load test statistics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Local metrics
        metrics.put("localSuccessCount", successCount.get());
        metrics.put("localErrorCount", errorCount.get());
        metrics.put("localTotalProcessed", successCount.get() + errorCount.get());
        metrics.put("localSuccessRate", successCount.get() + errorCount.get() > 0 ? 
            String.format("%.2f", (double) successCount.get() / (successCount.get() + errorCount.get()) * 100) + "%" : "0%");
        
        // Kafka metrics
        Map<String, Object> kafkaMetrics = kafkaMonitoringService.getLoadTestMetrics();
        metrics.put("kafkaMetrics", kafkaMetrics);
        
        metrics.put("timestamp", new Date());
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/kafka-status")
    @Operation(summary = "Get detailed Kafka metrics", description = "View detailed Kafka performance and monitoring data")
    public ResponseEntity<Map<String, Object>> getKafkaStatus() {
        return ResponseEntity.ok(kafkaMonitoringService.getMetrics());
    }

    @PostMapping("/reset-metrics")
    @Operation(summary = "Reset all metrics", description = "Reset load test and Kafka monitoring metrics")
    public ResponseEntity<Map<String, Object>> resetMetrics() {
        successCount.set(0);
        errorCount.set(0);
        kafkaMonitoringService.resetMetrics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All metrics reset successfully");
        response.put("timestamp", new Date());
        
        return ResponseEntity.ok(response);
    }
}