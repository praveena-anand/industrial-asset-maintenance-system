package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.Employee;
import com.industrial.maintenance.entity.EmployeeStatus;
import com.industrial.maintenance.entity.Factory;
import com.industrial.maintenance.entity.Role;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String email,
        String department,
        Role role,
        Long factoryId,
        String factoryCode,
        String factoryName,
        EmployeeStatus status
) {

    public static EmployeeResponse from(Employee employee) {
        Factory factory = employee.getFactory();
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getRole(),
                factory != null ? factory.getId() : null,
                factory != null ? factory.getFactoryCode() : null,
                factory != null ? factory.getName() : null,
                employee.getStatus()
        );
    }
}