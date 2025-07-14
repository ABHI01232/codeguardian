package com.codeguardian.gitprocessor.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic commitAnalysisTopic() {
        return new NewTopic("commit-analysis", 3, (short) 1);
    }

    @Bean
    public NewTopic pullRequestAnalysisTopic() {
        return new NewTopic("pull-request-analysis", 3, (short) 1);
    }

    @Bean
    public NewTopic mergeRequestAnalysisTopic() {
        return new NewTopic("merge-request-analysis", 3, (short) 1);
    }

    @Bean
    public NewTopic codeAnalysisResultsTopic() {
        return new NewTopic("code-analysis-results", 3, (short) 1);
    }

    @Bean
    public NewTopic notificationsTopic() {
        return new NewTopic("notifications", 3, (short) 1);
    }
}