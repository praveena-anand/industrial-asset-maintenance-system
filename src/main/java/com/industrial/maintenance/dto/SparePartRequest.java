package com.industrial.maintenance.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SparePartRequest(

        @NotBlank(message = "Part number is required")
        String partNumber,

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Unit price is required")
        @PositiveOrZero(message = "Unit price cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Unit price can have at most 2 decimal places")
        BigDecimal unitPrice
) {}