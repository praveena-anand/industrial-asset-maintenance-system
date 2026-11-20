package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.EquipmentCostSummary;
import com.industrial.maintenance.dto.MaintenanceRecordRequest;
import com.industrial.maintenance.entity.MaintenanceRecord;
import com.industrial.maintenance.service.MaintenanceRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MaintenanceRecordController {

    private final MaintenanceRecordService recordService;

    public MaintenanceRecordController(MaintenanceRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/maintenance-records")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRecord create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return recordService.createRecord(request);
    }

    @GetMapping("/maintenance-records/{id}")
    public MaintenanceRecord getById(@PathVariable Long id) {
        return recordService.getRecordById(id);
    }

    @GetMapping("/equipment/{equipmentId}/maintenance-history")
    public List<MaintenanceRecord> getHistory(@PathVariable Long equipmentId) {
        return recordService.getHistory(equipmentId);
    }

    @GetMapping("/equipment/{equipmentId}/maintenance-cost")
    public EquipmentCostSummary getCost(@PathVariable Long equipmentId) {
        return recordService.getCostSummary(equipmentId);
    }
}