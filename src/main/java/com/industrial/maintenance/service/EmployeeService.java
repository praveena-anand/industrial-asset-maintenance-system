package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.EmployeeRequest;
import com.industrial.maintenance.entity.Employee;
import com.industrial.maintenance.entity.EmployeeStatus;
import com.industrial.maintenance.entity.Role;
import com.industrial.maintenance.exception.DuplicateResourceException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import com.industrial.maintenance.exception.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final FactoryService factoryService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           FactoryService factoryService,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.factoryService = factoryService;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new DuplicateResourceException(
                    "Employee code " + request.employeeCode() + " already exists");
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "Email " + request.email() + " is already registered");
        }
        Employee employee = new Employee();
        applyRequest(employee, request);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employeeRepository.save(employee);
    }

    public List<Employee> getEmployees(Role role, String name) {
        if (role != null) {
            return employeeRepository.findByRole(role);
        }
        if (name != null && !name.isBlank()) {
            return employeeRepository.findByFullNameContainingIgnoreCase(name);
        }
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee with ID " + id + " not found"));
    }

    public Employee getActiveTechnician(Long id) {
        Employee employee = getEmployeeById(id);
        if (employee.getRole() != Role.TECHNICIAN) {
            throw new BusinessRuleException(employee.getFullName() + " is not a technician");
        }
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessRuleException(employee.getFullName() + " is not an active employee");
        }
        return employee;
    }

    public Employee updateEmployee(Long id, EmployeeRequest request) {
        Employee existing = getEmployeeById(id);
        if (employeeRepository.existsByEmployeeCodeAndIdNot(request.employeeCode(), id)) {
            throw new DuplicateResourceException(
                    "Employee code " + request.employeeCode() + " already exists");
        }
        if (employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException(
                    "Email " + request.email() + " is already registered");
        }
        applyRequest(existing, request);
        return employeeRepository.save(existing);
    }

    public void setPassword(Long id, String rawPassword) {
        Employee employee = getEmployeeById(id);
        employee.setPasswordHash(passwordEncoder.encode(rawPassword));
        employeeRepository.save(employee);
    }

    public void deactivateEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }

    private void applyRequest(Employee employee, EmployeeRequest request) {
        employee.setEmployeeCode(request.employeeCode());
        employee.setFullName(request.fullName());
        employee.setEmail(request.email());
        employee.setDepartment(request.department());
        employee.setRole(request.role());
        employee.setFactory(factoryService.getFactoryById(request.factoryId()));
    }
}