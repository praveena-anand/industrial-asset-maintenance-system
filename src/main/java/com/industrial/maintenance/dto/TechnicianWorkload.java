package com.industrial.maintenance.dto;

public record TechnicianWorkload(
        String technicianName,
        Long activeRequests
) {}