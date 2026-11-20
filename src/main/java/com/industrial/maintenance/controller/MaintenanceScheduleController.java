package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.MaintenanceScheduleRequest;
import com.industrial.maintenance.dto.MaintenanceScheduleResponse;
import com.industrial.maintenance.service.MaintenanceScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Maintenance Schedules", description = "Plan preventive maintenance and track overdue tasks")
@RestController
@RequestMapping("/api/maintenance-schedules")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;

    public MaintenanceScheduleController(MaintenanceScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceScheduleResponse create(@Valid @RequestBody MaintenanceScheduleRequest request) {
        return MaintenanceScheduleResponse.from(scheduleService.createSchedule(request));
    }

    @GetMapping
    public List<MaintenanceScheduleResponse> getAll(@RequestParam(required = false) Long equipmentId) {
        return scheduleService.getSchedules(equipmentId).stream()
                .map(MaintenanceScheduleResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MaintenanceScheduleResponse getById(@PathVariable Long id) {
        return MaintenanceScheduleResponse.from(scheduleService.getScheduleById(id));
    }

    @GetMapping("/overdue")
    public List<MaintenanceScheduleResponse> getOverdue() {
        return scheduleService.getOverdueSchedules().stream()
                .map(MaintenanceScheduleResponse::from)
                .toList();
    }

    @GetMapping("/upcoming")
    public List<MaintenanceScheduleResponse> getUpcoming(@RequestParam(defaultValue = "30") int days) {
        return scheduleService.getUpcomingSchedules(days).stream()
                .map(MaintenanceScheduleResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/assign")
    public MaintenanceScheduleResponse assign(@PathVariable Long id, @RequestParam Long technicianId) {
        return MaintenanceScheduleResponse.from(scheduleService.assignTechnician(id, technicianId));
    }

    @PostMapping("/{id}/complete")
    public MaintenanceScheduleResponse complete(@PathVariable Long id) {
        return MaintenanceScheduleResponse.from(scheduleService.completeSchedule(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        scheduleService.deactivateSchedule(id);
    }
}