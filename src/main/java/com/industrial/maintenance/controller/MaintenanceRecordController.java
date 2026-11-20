package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.EquipmentCostSummary;
import com.industrial.maintenance.dto.MaintenanceRecordRequest;
import com.industrial.maintenance.dto.MaintenanceRecordResponse;
import com.industrial.maintenance.service.MaintenanceRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Maintenance History", description = "Record completed work, parts used and costs")
@RestController
@RequestMapping("/api")
public class MaintenanceRecordController {

    private final MaintenanceRecordService recordService;

    public MaintenanceRecordController(MaintenanceRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/maintenance-records")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRecordResponse create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return MaintenanceRecordResponse.from(recordService.createRecord(request));
    }

    @GetMapping("/maintenance-records/{id}")
    public MaintenanceRecordResponse getById(@PathVariable Long id) {
        return MaintenanceRecordResponse.from(recordService.getRecordById(id));
    }

    @GetMapping("/equipment/{equipmentId}/maintenance-history")
    public List<MaintenanceRecordResponse> getHistory(@PathVariable Long equipmentId) {
        return recordService.getHistory(equipmentId).stream()
                .map(MaintenanceRecordResponse::from)
                .toList();
    }

    @GetMapping("/equipment/{equipmentId}/maintenance-cost")
    public EquipmentCostSummary getCost(@PathVariable Long equipmentId) {
        return recordService.getCostSummary(equipmentId);
    }
}