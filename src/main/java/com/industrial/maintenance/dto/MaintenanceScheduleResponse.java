package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Employee;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.MaintenanceSchedule;

import java.time.LocalDate;

public record MaintenanceScheduleResponse(
        Long id,
        Long equipmentId,
        String equipmentCode,
        String equipmentName,
        String taskName,
        String description,
        Integer frequencyDays,
        LocalDate nextDueDate,
        LocalDate lastCompletedDate,
        Double frequencyHours,
        Double nextDueHours,
        Double lastCompletedHours,
        Double currentOperatingHours,
        String technicianCode,
        String technicianName,
        boolean active,
        boolean overdue,
        String dueReason          // "DATE", "HOURS", "DATE_AND_HOURS" or null
) {

    public static MaintenanceScheduleResponse from(MaintenanceSchedule schedule, Double currentHours) {
        Equipment equipment = schedule.getEquipment();
        Employee technician = schedule.getAssignedTechnician();

        LocalDate today = LocalDate.now();
        boolean dueByDate = schedule.isDueByDate(today);
        boolean dueByHours = schedule.isDueByHours(currentHours);
        String dueReason = dueByDate && dueByHours ? "DATE_AND_HOURS"
                : dueByDate ? "DATE"
                : dueByHours ? "HOURS"
                : null;

        return new MaintenanceScheduleResponse(
                schedule.getId(),
                equipment != null ? equipment.getId() : null,
                equipment != null ? equipment.getEquipmentCode() : null,
                equipment != null ? equipment.getName() : null,
                schedule.getTaskName(),
                schedule.getDescription(),
                schedule.getFrequencyDays(),
                schedule.getNextDueDate(),
                schedule.getLastCompletedDate(),
                schedule.getFrequencyHours(),
                schedule.getNextDueHours(),
                schedule.getLastCompletedHours(),
                currentHours,
                technician != null ? technician.getEmployeeCode() : null,
                technician != null ? technician.getFullName() : null,
                schedule.isActive(),
                dueByDate || dueByHours,
                dueReason
        );
    }
}