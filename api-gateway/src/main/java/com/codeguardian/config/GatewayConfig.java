package com.codeguardian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Simplified Gateway Configuration
 * 
 * Note: Gateway routing is disabled for now to avoid WebFlux configuration conflicts.
 * The API Gateway is running as a standalone service with its own controllers.
 * 
 * Best Practice for Production:
 * 1. Use proper service discovery (Eureka, Consul)
 * 2. Implement circuit breakers
 * 3. Add load balancing
 * 4. Configure proper routing with path rewriting
 * 5. Add monitoring and metrics
 * 
 * Current state: Direct controller-based API without routing
 */
@Configuration
public class GatewayConfig {

    // Gateway routing disabled to avoid WebFlux conflicts
    // Controllers are directly exposed on this service
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }
}