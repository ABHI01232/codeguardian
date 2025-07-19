package com.codeguardian.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS configuration moved to GatewayConfig to avoid bean conflicts
 * 
 * Best Practice for Production:
 * 1. Restrict origins to specific domains only
 * 2. Limit allowed methods to necessary ones
 * 3. Restrict headers to required ones
 * 4. Set appropriate max age for preflight requests
 * 5. Enable credentials only when necessary
 * 
 * Current state: Configured in GatewayConfig
 */
@Configuration
public class CorsConfig {
    
    // CORS configuration moved to GatewayConfig.java
    // to avoid bean name conflicts
    
}