package com.industrial.maintenance.dto;

import jakarta.validation.constraints.NotBlank;

public record FactoryRequest(

        @NotBlank(message = "Factory code is required")
        String factoryCode,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "City is required")
        String city,

        String address
) {}