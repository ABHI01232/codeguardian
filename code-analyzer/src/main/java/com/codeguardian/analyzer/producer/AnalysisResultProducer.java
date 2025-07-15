package com.codeguardian.analyzer.producer;

import com.codeguardian.analyzer.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.analysis-results}")
    private String analysisResultsTopic;

    public void sendResult(AnalysisResult result) {
        try {
            log.info("Sending analysis result for commit: {} to topic: {}", 
                result.getCommitHash(), analysisResultsTopic);
            
            kafkaTemplate.send(analysisResultsTopic, result.getCommitHash(), result)
                .whenComplete((sendResult, ex) -> {
                    if (ex == null) {
                        log.info("Analysis result sent successfully for commit: {} with offset: {}", 
                            result.getCommitHash(), sendResult.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send analysis result for commit: {}", 
                            result.getCommitHash(), ex);
                    }
                });
        } catch (Exception e) {
            log.error("Error sending analysis result: {}", e.getMessage(), e);
        }
    }
}