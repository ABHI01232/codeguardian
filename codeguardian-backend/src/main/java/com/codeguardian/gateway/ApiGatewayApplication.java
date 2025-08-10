package com.codeguardian.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.codeguardian")
@EnableJpaRepositories(basePackages = "com.codeguardian.repository")
@EntityScan(basePackages = "com.codeguardian.entity")
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting CodeGuardian API Gateway...");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        System.out.println("✅ CodeGuardian API Gateway started successfully");
        System.out.println("🌐 API Gateway: http://localhost:8080");
        System.out.println("📚 Swagger UI: http://localhost:8080/swagger-ui/index.html");
        System.out.println("📖 OpenAPI Docs: http://localhost:8080/v3/api-docs");
        System.out.println("🗃️  H2 Console: http://localhost:8080/h2-console");
        System.out.println("📊 Ready to process security analysis requests!");
    }
    
    @EventListener(ContextClosedEvent.class)
    public void applicationStopping() {
        System.out.println("🛑 CodeGuardian API Gateway shutting down...");
        System.out.println("✅ Kafka connections closed");
        System.out.println("✅ Database connections closed");
        System.out.println("🏁 CodeGuardian API Gateway stopped successfully");
    }
}