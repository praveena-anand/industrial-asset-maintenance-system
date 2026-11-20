package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.EquipmentStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EquipmentRequest(

        @NotBlank(message = "Equipment code is required")
        String equipmentCode,

        @NotBlank(message = "Name is required")
        String name,

        String model,

        @NotBlank(message = "Serial number is required")
        String serialNumber,

        @NotNull(message = "Factory ID is required")
        Long factoryId,

        @PastOrPresent(message = "Purchase date cannot be in the future")
        LocalDate purchaseDate,

        EquipmentStatus status
) {}