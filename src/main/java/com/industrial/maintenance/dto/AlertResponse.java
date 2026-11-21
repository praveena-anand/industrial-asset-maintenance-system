package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.*;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long equipmentId,
        String equipmentCode,
        AlertType type,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        Double triggerValue,
        Double thresholdValue,
        Long serviceRequestId,
        String acknowledgedByCode,
        LocalDateTime createdAt,
        LocalDateTime acknowledgedAt,
        LocalDateTime resolvedAt
) {

    public static AlertResponse from(Alert alert) {
        Employee acknowledgedBy = alert.getAcknowledgedBy();
        return new AlertResponse(
                alert.getId(),
                alert.getEquipment().getId(),
                alert.getEquipment().getEquipmentCode(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getTriggerValue(),
                alert.getThresholdValue(),
                alert.getServiceRequest() != null ? alert.getServiceRequest().getId() : null,
                acknowledgedBy != null ? acknowledgedBy.getEmployeeCode() : null,
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt()
        );
    }
}