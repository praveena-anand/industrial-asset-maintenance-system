package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.entity.Factory;

import java.time.LocalDate;

public record EquipmentResponse(
        Long id,
        String equipmentCode,
        String name,
        String model,
        String serialNumber,
        Long factoryId,
        String factoryCode,
        String factoryName,
        LocalDate purchaseDate,
        EquipmentStatus status
) {

    public static EquipmentResponse from(Equipment equipment) {
        Factory factory = equipment.getFactory();
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getEquipmentCode(),
                equipment.getName(),
                equipment.getModel(),
                equipment.getSerialNumber(),
                factory != null ? factory.getId() : null,
                factory != null ? factory.getFactoryCode() : null,
                factory != null ? factory.getName() : null,
                equipment.getPurchaseDate(),
                equipment.getStatus()
        );
    }
}