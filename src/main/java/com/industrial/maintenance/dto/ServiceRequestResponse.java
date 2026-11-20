package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.*;

import java.time.LocalDateTime;

public record ServiceRequestResponse(
        Long id,
        Long equipmentId,
        String equipmentCode,
        String equipmentName,
        String problemDescription,
        Priority priority,
        ServiceRequestStatus status,
        String reportedByCode,
        String reportedByName,
        String technicianCode,
        String technicianName,
        String resolutionNotes,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Long resolutionMinutes
) {

    public static ServiceRequestResponse from(ServiceRequest request) {
        Equipment equipment = request.getEquipment();
        Employee reporter = request.getReportedBy();
        Employee technician = request.getAssignedTechnician();

        return new ServiceRequestResponse(
                request.getId(),
                equipment != null ? equipment.getId() : null,
                equipment != null ? equipment.getEquipmentCode() : null,
                equipment != null ? equipment.getName() : null,
                request.getProblemDescription(),
                request.getPriority(),
                request.getStatus(),
                reporter != null ? reporter.getEmployeeCode() : null,
                reporter != null ? reporter.getFullName() : null,
                technician != null ? technician.getEmployeeCode() : null,
                technician != null ? technician.getFullName() : null,
                request.getResolutionNotes(),
                request.getCreatedAt(),
                request.getCompletedAt(),
                request.getResolutionMinutes()
        );
    }
}