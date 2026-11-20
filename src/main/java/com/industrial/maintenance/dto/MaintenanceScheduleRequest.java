package com.industrial.maintenance.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MaintenanceScheduleRequest(

        @NotNull(message = "Equipment ID is required")
        Long equipmentId,

        @NotBlank(message = "Task name is required")
        String taskName,

        String description,

        @NotNull(message = "Frequency is required")
        @Positive(message = "Frequency must be greater than 0")
        Integer frequencyDays,

        @NotNull(message = "Next due date is required")
        LocalDate nextDueDate,

        Long technicianId
) {}