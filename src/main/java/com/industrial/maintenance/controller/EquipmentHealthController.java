package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.EquipmentHealthResponse;
import com.industrial.maintenance.service.EquipmentHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Equipment Health", description = "Health scores calculated from sensors, alerts, maintenance and breakdowns")
@RestController
@RequestMapping("/api/equipment")
public class EquipmentHealthController {

    private final EquipmentHealthService healthService;

    public EquipmentHealthController(EquipmentHealthService healthService) {
        this.healthService = healthService;
    }

    @Operation(summary = "Health of all active machines, worst first")
    @GetMapping("/health")
    public List<EquipmentHealthResponse> overview() {
        return healthService.getHealthOverview();
    }

    @Operation(summary = "Health score of one machine, with the reasons for any deductions")
    @GetMapping("/{equipmentId}/health")
    public EquipmentHealthResponse health(@PathVariable Long equipmentId) {
        return healthService.getHealth(equipmentId);
    }
}