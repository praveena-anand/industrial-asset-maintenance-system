package com.industrial.maintenance.dto;

public record EquipmentBreakdownCount(
        String equipmentCode,
        String equipmentName,
        Long breakdownCount
) {}