package com.industrial.maintenance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PartUsageRequest(

        @NotNull(message = "Spare part ID is required")
        Long sparePartId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {}