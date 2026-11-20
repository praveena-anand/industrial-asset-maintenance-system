package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.EquipmentRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.exception.DuplicateEquipmentException;
import com.industrial.maintenance.exception.EquipmentNotFoundException;
import com.industrial.maintenance.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.industrial.maintenance.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
@Import(SecurityConfig.class)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentService equipmentService;

    private static final String VALID_JSON = """
            {
              "equipmentCode": "EQ2001",
              "name": "Wheel Loader",
              "model": "950GC",
              "serialNumber": "SER2001",
              "factoryId": 1,
              "purchaseDate": "2026-01-01"
            }
            """;

    @Test
    void register_withValidData_returns201() throws Exception {
        Equipment saved = new Equipment();
        saved.setId(1L);
        saved.setEquipmentCode("EQ2001");
        saved.setName("Wheel Loader");
        saved.setStatus(EquipmentStatus.OPERATIONAL);
        when(equipmentService.registerEquipment(any(EquipmentRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.equipmentCode").value("EQ2001"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"));
    }

    @Test
    void register_withMissingName_returns400() throws Exception {
        String invalidJson = """
                { "equipmentCode": "EQ2001", "serialNumber": "SER2001", "factoryId": 1 }
                """;

        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("name: Name is required")));

        verify(equipmentService, never()).registerEquipment(any());
    }

    @Test
    void register_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Broken\" "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void register_withDuplicateCode_returns409() throws Exception {
        when(equipmentService.registerEquipment(any(EquipmentRequest.class)))
                .thenThrow(new DuplicateEquipmentException("Equipment code EQ2001 already exists"));

        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Equipment code EQ2001 already exists"))
                .andExpect(jsonPath("$.path").value("/api/equipment"));
    }

    @Test
    void getById_whenMissing_returns404() throws Exception {
        when(equipmentService.getEquipmentById(99L))
                .thenThrow(new EquipmentNotFoundException("Equipment with ID 99 not found"));

        mockMvc.perform(get("/api/equipment/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Equipment with ID 99 not found"));
    }

    @Test
    void getById_withNonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/equipment/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'id'"));
    }
}