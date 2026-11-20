package com.industrial.maintenance.dto;

import java.math.BigDecimal;

public record EquipmentCostSummary(
        Long equipmentId,
        String equipmentCode,
        long numberOfRecords,
        BigDecimal totalMaintenanceCost
) {}