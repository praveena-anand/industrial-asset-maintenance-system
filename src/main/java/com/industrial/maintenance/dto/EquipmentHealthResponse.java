package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.HealthStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EquipmentHealthResponse(
        Long equipmentId,
        String equipmentCode,
        String equipmentName,
        int score,
        HealthStatus status,
        LocalDateTime lastReadingAt,
        List<String> factors
) {}