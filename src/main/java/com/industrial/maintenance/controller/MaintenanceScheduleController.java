package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.MaintenanceScheduleRequest;
import com.industrial.maintenance.dto.MaintenanceScheduleResponse;
import com.industrial.maintenance.entity.MaintenanceSchedule;
import com.industrial.maintenance.service.MaintenanceScheduleService;
import io.swagger.v3.oas.annotations.Operation;
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

    private MaintenanceScheduleResponse toResponse(MaintenanceSchedule schedule) {
        return MaintenanceScheduleResponse.from(schedule, scheduleService.getCurrentHours(schedule));
    }

    private List<MaintenanceScheduleResponse> toResponses(List<MaintenanceSchedule> schedules) {
        return schedules.stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceScheduleResponse create(@Valid @RequestBody MaintenanceScheduleRequest request) {
        return toResponse(scheduleService.createSchedule(request));
    }

    @GetMapping
    public List<MaintenanceScheduleResponse> getAll(@RequestParam(required = false) Long equipmentId) {
        return toResponses(scheduleService.getSchedules(equipmentId));
    }

    @GetMapping("/{id}")
    public MaintenanceScheduleResponse getById(@PathVariable Long id) {
        return toResponse(scheduleService.getScheduleById(id));
    }

    @Operation(summary = "Active schedules overdue by date or by operating hours")
    @GetMapping("/overdue")
    public List<MaintenanceScheduleResponse> getOverdue() {
        return toResponses(scheduleService.getOverdueSchedules());
    }

    @GetMapping("/upcoming")
    public List<MaintenanceScheduleResponse> getUpcoming(@RequestParam(defaultValue = "30") int days) {
        return toResponses(scheduleService.getUpcomingSchedules(days));
    }

    @PatchMapping("/{id}/assign")
    public MaintenanceScheduleResponse assign(@PathVariable Long id, @RequestParam Long technicianId) {
        return toResponse(scheduleService.assignTechnician(id, technicianId));
    }

    @PostMapping("/{id}/complete")
    public MaintenanceScheduleResponse complete(@PathVariable Long id) {
        return toResponse(scheduleService.completeSchedule(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        scheduleService.deactivateSchedule(id);
    }
}