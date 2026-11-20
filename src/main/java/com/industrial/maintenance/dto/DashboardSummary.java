package com.industrial.maintenance.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummary(
        long totalEquipment,
        long operationalEquipment,
        long equipmentUnderMaintenance,
        long openServiceRequests,
        long overdueMaintenanceTasks,
        BigDecimal totalMaintenanceExpenditure,
        List<EquipmentBreakdownCount> mostFrequentlyFailingEquipment,
        List<TechnicianWorkload> technicianWorkload
) {}