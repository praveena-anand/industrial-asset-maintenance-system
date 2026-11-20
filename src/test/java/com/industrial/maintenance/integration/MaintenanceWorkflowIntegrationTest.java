package com.industrial.maintenance.integration;

import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.repository.EmployeeRepository;
import com.industrial.maintenance.repository.EquipmentRepository;
import com.industrial.maintenance.repository.FactoryRepository;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MaintenanceWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private FactoryRepository factoryRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private ServiceRequestRepository serviceRequestRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Factory factory;
    private Employee operator;
    private Employee technician;

    private static RequestPostProcessor asRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @BeforeEach
    void setUp() {
        factory = new Factory();
        factory.setFactoryCode("FAC-TEST");
        factory.setName("Test Plant");
        factory.setCity("Chennai");
        factory = factoryRepository.save(factory);

        operator = createEmployee("EMP9001", "operator@test.com", Role.OPERATOR);
        technician = createEmployee("EMP9002", "technician@test.com", Role.TECHNICIAN);
    }

    @Test
    void registerEquipment_isSavedToDatabase_andDuplicateIsRejected() throws Exception {
        String json = """
                {
                  "equipmentCode": "EQ9001",
                  "name": "Test Excavator",
                  "serialNumber": "TEST-SER-9001",
                  "factoryId": %d,
                  "purchaseDate": "2026-01-01"
                }
                """.formatted(factory.getId());

        mockMvc.perform(post("/api/equipment")
                        .with(asRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPERATIONAL"))
                .andExpect(jsonPath("$.factoryCode").value("FAC-TEST"));

        assertTrue(equipmentRepository.existsByEquipmentCode("EQ9001"));

        mockMvc.perform(post("/api/equipment")
                        .with(asRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void breakdownWorkflow_updatesEquipmentStatusInDatabase() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setEquipmentCode("EQ9002");
        equipment.setName("Test Bulldozer");
        equipment.setSerialNumber("TEST-SER-9002");
        equipment.setFactory(factory);
        equipment.setPurchaseDate(LocalDate.of(2026, 1, 1));
        equipment.setStatus(EquipmentStatus.OPERATIONAL);
        equipment = equipmentRepository.save(equipment);

        // 1. Operator reports a breakdown
        String reportJson = """
                { "equipmentId": %d, "reportedById": %d,
                  "problemDescription": "Engine overheating", "priority": "HIGH" }
                """.formatted(equipment.getId(), operator.getId());

        mockMvc.perform(post("/api/service-requests")
                        .with(asRole("OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        Long requestId = serviceRequestRepository.findByEquipmentId(equipment.getId()).get(0).getId();

        // 2. Manager assigns a technician
        mockMvc.perform(patch("/api/service-requests/{id}/assign", requestId)
                        .with(asRole("MANAGER"))
                        .param("technicianId", technician.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        // 3. Technician starts work: the machine must be under maintenance
        mockMvc.perform(post("/api/service-requests/{id}/start", requestId)
                        .with(asRole("TECHNICIAN")))
                .andExpect(status().isOk());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE,
                equipmentRepository.findById(equipment.getId()).orElseThrow().getStatus());

        // 4. Technician completes work: the machine must be operational again
        mockMvc.perform(post("/api/service-requests/{id}/complete", requestId)
                        .with(asRole("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "resolutionNotes": "Replaced thermostat" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        assertEquals(EquipmentStatus.OPERATIONAL,
                equipmentRepository.findById(equipment.getId()).orElseThrow().getStatus());
    }

    @Test
    void realLogin_returnsTokenThatGrantsAccessByRole() throws Exception {
        technician.setPasswordHash(passwordEncoder.encode("Test@12345"));
        employeeRepository.save(technician);

        // Log in for real and read the token from the response
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "technician@test.com", "password": "Test@12345" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TECHNICIAN"))
                .andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        // The token works for something a technician may do...
        mockMvc.perform(get("/api/equipment").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // ...is refused for something a technician may not do...
        mockMvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // ...and without any token the request is rejected
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createFactory_cannotOverwriteAnExistingFactoryById() throws Exception {
        String attack = """
                { "id": %d, "factoryCode": "FAC-OTHER", "name": "Overwritten", "city": "Elsewhere" }
                """.formatted(factory.getId());

        mockMvc.perform(post("/api/factories")
                .with(asRole("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(attack));

        Factory original = factoryRepository.findById(factory.getId()).orElseThrow();
        assertEquals("FAC-TEST", original.getFactoryCode());
        assertEquals("Test Plant", original.getName());
    }

    private Employee createEmployee(String code, String email, Role role) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFullName("Test " + role);
        employee.setEmail(email);
        employee.setRole(role);
        employee.setFactory(factory);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employeeRepository.save(employee);
    }
}