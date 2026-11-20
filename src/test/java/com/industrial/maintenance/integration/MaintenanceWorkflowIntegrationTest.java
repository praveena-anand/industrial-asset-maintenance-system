package com.industrial.maintenance.integration;

import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.repository.EmployeeRepository;
import com.industrial.maintenance.repository.EquipmentRepository;
import com.industrial.maintenance.repository.FactoryRepository;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
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

    private Factory factory;
    private Employee operator;
    private Employee technician;

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPERATIONAL"))
                .andExpect(jsonPath("$.factory.factoryCode").value("FAC-TEST"));

        assertTrue(equipmentRepository.existsByEquipmentCode("EQ9001"));

        mockMvc.perform(post("/api/equipment")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        Long requestId = serviceRequestRepository.findByEquipmentId(equipment.getId()).get(0).getId();

        // 2. Technician is assigned
        mockMvc.perform(patch("/api/service-requests/{id}/assign", requestId)
                        .param("technicianId", technician.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        // 3. Work starts: the machine must now be under maintenance
        mockMvc.perform(post("/api/service-requests/{id}/start", requestId))
                .andExpect(status().isOk());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE,
                equipmentRepository.findById(equipment.getId()).orElseThrow().getStatus());

        // 4. Work completes: the machine must be operational again
        mockMvc.perform(post("/api/service-requests/{id}/complete", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "resolutionNotes": "Replaced thermostat" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        assertEquals(EquipmentStatus.OPERATIONAL,
                equipmentRepository.findById(equipment.getId()).orElseThrow().getStatus());
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