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
        String technicianCode,
        String technicianName,
        boolean active,
        boolean overdue
) {

    public static MaintenanceScheduleResponse from(MaintenanceSchedule schedule) {
        Equipment equipment = schedule.getEquipment();
        Employee technician = schedule.getAssignedTechnician();

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
                technician != null ? technician.getEmployeeCode() : null,
                technician != null ? technician.getFullName() : null,
                schedule.isActive(),
                schedule.isDueByDate(LocalDate.now())    // hours are added in the next step
        );
    }
}