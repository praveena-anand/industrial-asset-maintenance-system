package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.EquipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Data required to register or update a piece of equipment")
public record EquipmentRequest(

        @Schema(description = "Unique equipment code", example = "EQ1001")
        @NotBlank(message = "Equipment code is required")
        String equipmentCode,

        @Schema(description = "Equipment name", example = "Hydraulic Excavator")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Manufacturer model", example = "EX-200")
        String model,

        @Schema(description = "Unique manufacturer serial number", example = "CATEX200001")
        @NotBlank(message = "Serial number is required")
        String serialNumber,

        @Schema(description = "ID of the factory where the equipment is located", example = "1")
        @NotNull(message = "Factory ID is required")
        Long factoryId,

        @Schema(description = "Date of purchase (cannot be in the future)", example = "2026-01-15")
        @PastOrPresent(message = "Purchase date cannot be in the future")
        LocalDate purchaseDate,

        @Schema(description = "Initial status; defaults to OPERATIONAL if omitted", example = "OPERATIONAL")
        EquipmentStatus status
) {}