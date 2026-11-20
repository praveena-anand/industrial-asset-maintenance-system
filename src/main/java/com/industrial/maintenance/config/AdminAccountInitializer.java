package com.industrial.maintenance.config;

import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.repository.EmployeeRepository;
import com.industrial.maintenance.repository.FactoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final FactoryRepository factoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(EmployeeRepository employeeRepository,
                                   FactoryRepository factoryRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${app.admin.email}") String adminEmail,
                                   @Value("${app.admin.password:}") String adminPassword) {
        this.employeeRepository = employeeRepository;
        this.factoryRepository = factoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.existsByRole(Role.ADMIN)) {
            log.debug("An ADMIN account already exists; skipping initial admin creation");
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("No ADMIN account exists and ADMIN_PASSWORD is not set; skipping initial admin creation");
            return;
        }

        Factory headOffice = factoryRepository.findByFactoryCode("FAC-HQ").orElseGet(() -> {
            Factory factory = new Factory();
            factory.setFactoryCode("FAC-HQ");
            factory.setName("Head Office");
            factory.setCity("Chennai");
            return factoryRepository.save(factory);
        });

        Employee admin = new Employee();
        admin.setEmployeeCode("EMP0001");
        admin.setFullName("System Administrator");
        admin.setEmail(adminEmail);
        admin.setDepartment("Administration");
        admin.setRole(Role.ADMIN);
        admin.setFactory(headOffice);
        admin.setStatus(EmployeeStatus.ACTIVE);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        employeeRepository.save(admin);

        log.info("Created initial ADMIN account {}", admin.getEmployeeCode());
    }
}