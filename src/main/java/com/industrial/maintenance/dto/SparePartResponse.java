package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.SparePart;

import java.math.BigDecimal;

public record SparePartResponse(
        Long id,
        String partNumber,
        String name,
        BigDecimal unitPrice
) {

    public static SparePartResponse from(SparePart part) {
        return new SparePartResponse(
                part.getId(),
                part.getPartNumber(),
                part.getName(),
                part.getUnitPrice()
        );
    }
}