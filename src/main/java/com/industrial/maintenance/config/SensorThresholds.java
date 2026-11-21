package com.industrial.maintenance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.thresholds")
public record SensorThresholds(
        double temperatureWarning,
        double temperatureCritical,
        double vibrationWarning,
        double vibrationCritical,
        double oilPressureWarning,
        double oilPressureCritical
) {}