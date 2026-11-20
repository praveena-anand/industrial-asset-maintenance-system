package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Role;
import jakarta.validation.constraints.*;

public record EmployeeRequest(

        @NotBlank(message = "Employee code is required")
        String employeeCode,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        String department,

        @NotNull(message = "Role is required")
        Role role,

        @NotNull(message = "Factory ID is required")
        Long factoryId
) {}