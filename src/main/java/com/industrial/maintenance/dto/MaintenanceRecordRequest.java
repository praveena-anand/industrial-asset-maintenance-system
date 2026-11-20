package com.industrial.maintenance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MaintenanceRecordRequest(

        @NotNull(message = "Equipment ID is required")
        Long equipmentId,

        @NotNull(message = "Technician ID is required")
        Long technicianId,

        Long serviceRequestId,

        Long scheduleId,

        @NotBlank(message = "Work description is required")
        @Size(max = 2000, message = "Work description must be at most 2000 characters")
        String workDescription,

        @NotNull(message = "Performed date is required")
        @PastOrPresent(message = "Performed date cannot be in the future")
        LocalDate performedDate,

        @NotNull(message = "Labour cost is required")
        @PositiveOrZero(message = "Labour cost cannot be negative")
        BigDecimal laborCost,

        List<@Valid PartUsageRequest> parts
) {}