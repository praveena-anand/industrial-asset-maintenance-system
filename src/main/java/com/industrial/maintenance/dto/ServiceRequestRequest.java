package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Priority;
import jakarta.validation.constraints.*;

public record ServiceRequestRequest(

        @NotNull(message = "Equipment ID is required")
        Long equipmentId,

        @NotNull(message = "Reporter ID is required")
        Long reportedById,

        @NotBlank(message = "Problem description is required")
        @Size(max = 1000, message = "Problem description must be at most 1000 characters")
        String problemDescription,

        @NotNull(message = "Priority is required")
        Priority priority
) {}