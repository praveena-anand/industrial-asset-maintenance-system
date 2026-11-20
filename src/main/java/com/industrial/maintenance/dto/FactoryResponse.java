package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Factory;

public record FactoryResponse(
        Long id,
        String factoryCode,
        String name,
        String city,
        String address
) {

    public static FactoryResponse from(Factory factory) {
        return new FactoryResponse(
                factory.getId(),
                factory.getFactoryCode(),
                factory.getName(),
                factory.getCity(),
                factory.getAddress()
        );
    }
}