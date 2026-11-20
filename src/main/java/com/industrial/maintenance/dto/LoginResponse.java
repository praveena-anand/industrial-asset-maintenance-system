package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Role;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String employeeCode,
        String fullName,
        Role role
) {}