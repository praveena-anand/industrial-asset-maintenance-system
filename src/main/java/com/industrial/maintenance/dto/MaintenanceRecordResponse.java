package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MaintenanceRecordResponse(
        Long id,
        Long equipmentId,
        String equipmentCode,
        String technicianCode,
        String technicianName,
        Long serviceRequestId,
        Long scheduleId,
        String workDescription,
        LocalDate performedDate,
        BigDecimal laborCost,
        List<PartLine> parts,
        BigDecimal totalCost
) {

    public record PartLine(
            String partNumber,
            String partName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
        static PartLine from(MaintenanceRecordPart line) {
            return new PartLine(
                    line.getSparePart().getPartNumber(),
                    line.getSparePart().getName(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getLineTotal()
            );
        }
    }

    public static MaintenanceRecordResponse from(MaintenanceRecord record) {
        Equipment equipment = record.getEquipment();
        Employee technician = record.getTechnician();

        return new MaintenanceRecordResponse(
                record.getId(),
                equipment.getId(),
                equipment.getEquipmentCode(),
                technician.getEmployeeCode(),
                technician.getFullName(),
                record.getServiceRequest() != null ? record.getServiceRequest().getId() : null,
                record.getMaintenanceSchedule() != null ? record.getMaintenanceSchedule().getId() : null,
                record.getWorkDescription(),
                record.getPerformedDate(),
                record.getLaborCost(),
                record.getParts().stream().map(PartLine::from).toList(),
                record.getTotalCost()
        );
    }
}