package com.industrial.maintenance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ml")
public record MlProperties(
        boolean enabled,
        String baseUrl,
        int timeoutMillis
) {}