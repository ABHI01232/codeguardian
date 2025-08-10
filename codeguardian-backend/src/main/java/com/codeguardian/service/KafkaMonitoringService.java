package com.codeguardian.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class KafkaMonitoringService {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Metrics tracking
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesReceived = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final Map<String, AtomicLong> topicMetrics = new HashMap<>();
    private final List<String> recentMessages = new ArrayList<>();
    private final int MAX_RECENT_MESSAGES = 100;
    
    // Performance tracking
    private long startTime = System.currentTimeMillis();
    private final List<Long> responseTimes = new ArrayList<>();

    public void recordMessageSent(String topic) {
        totalMessagesSent.incrementAndGet();
        topicMetrics.computeIfAbsent(topic + "_sent", k -> new AtomicLong(0)).incrementAndGet();
        
        synchronized (recentMessages) {
            recentMessages.add(0, "[SENT] " + topic + " at " + new Date());
            if (recentMessages.size() > MAX_RECENT_MESSAGES) {
                recentMessages.remove(recentMessages.size() - 1);
            }
        }
    }

    public void recordMessageReceived(String topic, long processingTime) {
        totalMessagesReceived.incrementAndGet();
        topicMetrics.computeIfAbsent(topic + "_received", k -> new AtomicLong(0)).incrementAndGet();
        
        synchronized (responseTimes) {
            responseTimes.add(processingTime);
            if (responseTimes.size() > 1000) {
                responseTimes.remove(0);
            }
        }
        
        synchronized (recentMessages) {
            recentMessages.add(0, "[RECEIVED] " + topic + " processed in " + processingTime + "ms at " + new Date());
            if (recentMessages.size() > MAX_RECENT_MESSAGES) {
                recentMessages.remove(recentMessages.size() - 1);
            }
        }
    }

    public void recordError(String topic, String error) {
        totalErrors.incrementAndGet();
        topicMetrics.computeIfAbsent(topic + "_errors", k -> new AtomicLong(0)).incrementAndGet();
        
        synchronized (recentMessages) {
            recentMessages.add(0, "[ERROR] " + topic + " - " + error + " at " + new Date());
            if (recentMessages.size() > MAX_RECENT_MESSAGES) {
                recentMessages.remove(recentMessages.size() - 1);
            }
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Basic metrics
        metrics.put("totalMessagesSent", totalMessagesSent.get());
        metrics.put("totalMessagesReceived", totalMessagesReceived.get());
        metrics.put("totalErrors", totalErrors.get());
        metrics.put("uptime", System.currentTimeMillis() - startTime);
        
        // Performance metrics
        if (!responseTimes.isEmpty()) {
            synchronized (responseTimes) {
                double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
                long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
                
                metrics.put("averageResponseTime", String.format("%.2f ms", avgResponseTime));
                metrics.put("maxResponseTime", maxResponseTime + " ms");
                metrics.put("minResponseTime", minResponseTime + " ms");
            }
        }
        
        // Throughput metrics
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (uptimeSeconds > 0) {
            double messagesPerSecond = (double) totalMessagesSent.get() / uptimeSeconds;
            metrics.put("throughputPerSecond", String.format("%.2f msg/sec", messagesPerSecond));
        }
        
        // Topic-specific metrics
        Map<String, Object> topics = new HashMap<>();
        for (Map.Entry<String, AtomicLong> entry : topicMetrics.entrySet()) {
            topics.put(entry.getKey(), entry.getValue().get());
        }
        metrics.put("topicMetrics", topics);
        
        // Recent activity
        synchronized (recentMessages) {
            metrics.put("recentMessages", new ArrayList<>(recentMessages));
        }
        
        // Kafka status
        metrics.put("kafkaAvailable", kafkaTemplate != null);
        metrics.put("timestamp", new Date());
        
        return metrics;
    }

    public Map<String, Object> getLoadTestMetrics() {
        Map<String, Object> loadMetrics = new HashMap<>();
        
        long currentTime = System.currentTimeMillis();
        long testDuration = currentTime - startTime;
        
        // Load test specific metrics
        loadMetrics.put("testDurationMs", testDuration);
        loadMetrics.put("testDurationSeconds", testDuration / 1000);
        loadMetrics.put("totalRequests", totalMessagesSent.get());
        loadMetrics.put("successfulRequests", totalMessagesReceived.get());
        loadMetrics.put("failedRequests", totalErrors.get());
        
        // Success rate
        long total = totalMessagesSent.get();
        if (total > 0) {
            double successRate = (double) totalMessagesReceived.get() / total * 100;
            loadMetrics.put("successRate", String.format("%.2f%%", successRate));
        } else {
            loadMetrics.put("successRate", "0%");
        }
        
        // Throughput calculations
        if (testDuration > 0) {
            double requestsPerSecond = (double) total / (testDuration / 1000.0);
            double messagesPerSecond = (double) totalMessagesReceived.get() / (testDuration / 1000.0);
            
            loadMetrics.put("requestThroughput", String.format("%.2f req/sec", requestsPerSecond));
            loadMetrics.put("processingThroughput", String.format("%.2f msg/sec", messagesPerSecond));
        }
        
        // Performance percentiles
        if (!responseTimes.isEmpty()) {
            synchronized (responseTimes) {
                List<Long> sortedTimes = new ArrayList<>(responseTimes);
                sortedTimes.sort(Long::compareTo);
                
                int size = sortedTimes.size();
                loadMetrics.put("p50ResponseTime", sortedTimes.get(size / 2) + " ms");
                loadMetrics.put("p90ResponseTime", sortedTimes.get((int) (size * 0.9)) + " ms");
                loadMetrics.put("p95ResponseTime", sortedTimes.get((int) (size * 0.95)) + " ms");
                loadMetrics.put("p99ResponseTime", sortedTimes.get((int) (size * 0.99)) + " ms");
            }
        }
        
        return loadMetrics;
    }

    public void resetMetrics() {
        totalMessagesSent.set(0);
        totalMessagesReceived.set(0);
        totalErrors.set(0);
        topicMetrics.clear();
        responseTimes.clear();
        synchronized (recentMessages) {
            recentMessages.clear();
        }
        startTime = System.currentTimeMillis();
        
        System.out.println("📊 Kafka monitoring metrics reset");
    }

    public void printCurrentStats() {
        System.out.println("📈 Current Kafka Stats:");
        System.out.println("   • Messages Sent: " + totalMessagesSent.get());
        System.out.println("   • Messages Received: " + totalMessagesReceived.get());
        System.out.println("   • Errors: " + totalErrors.get());
        
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (uptimeSeconds > 0) {
            double throughput = (double) totalMessagesSent.get() / uptimeSeconds;
            System.out.println("   • Throughput: " + String.format("%.2f", throughput) + " msg/sec");
        }
    }
}