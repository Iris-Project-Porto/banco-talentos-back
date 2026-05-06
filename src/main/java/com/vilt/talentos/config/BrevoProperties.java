package com.vilt.talentos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.brevo")
public record BrevoProperties(
        String apiKey,
        String fromEmail,
        String fromName
) {
}
