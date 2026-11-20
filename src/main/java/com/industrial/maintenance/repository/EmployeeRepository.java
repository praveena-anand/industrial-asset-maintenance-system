package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.Employee;
import com.industrial.maintenance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    List<Employee> findByRole(Role role);

    List<Employee> findByFullNameContainingIgnoreCase(String name);

    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}