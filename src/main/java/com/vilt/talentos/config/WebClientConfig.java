package com.vilt.talentos.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {
    private final BrevoProperties brevoProperties;

    @Bean
    public WebClient brevoWebClient(WebClient.Builder builder) {
        log.info("Inicializando Brevo WebClient...");
        return builder
            .baseUrl("https://api.brevo.com/v3/smtp/email")
            .defaultHeader("api-key", brevoProperties.apiKey())
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
