package com.codeguardian.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CodeAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeAnalyzerApplication.class, args);
    }
}