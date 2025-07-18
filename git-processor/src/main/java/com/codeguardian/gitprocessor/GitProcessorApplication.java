package com.codeguardian.gitprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class GitProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitProcessorApplication.class, args);
    }
}
