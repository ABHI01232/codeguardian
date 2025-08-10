package com.codeguardian.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI codeGuardianOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodeGuardian API")
                        .description("Real-Time Security Analysis Platform for Banking Applications")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CodeGuardian Team")
                                .email("support@codeguardian.com")
                                .url("https://github.com/codeguardian")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.codeguardian.com").description("Production Server")
                ));
    }
}