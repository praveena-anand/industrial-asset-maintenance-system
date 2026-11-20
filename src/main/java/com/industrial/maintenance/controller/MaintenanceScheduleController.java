package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.MaintenanceScheduleRequest;
import com.industrial.maintenance.entity.MaintenanceSchedule;
import com.industrial.maintenance.service.MaintenanceScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-schedules")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;

    public MaintenanceScheduleController(MaintenanceScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceSchedule create(@Valid @RequestBody MaintenanceScheduleRequest request) {
        return scheduleService.createSchedule(request);
    }

    @GetMapping
    public List<MaintenanceSchedule> getAll(@RequestParam(required = false) Long equipmentId) {
        return scheduleService.getSchedules(equipmentId);
    }

    @GetMapping("/{id}")
    public MaintenanceSchedule getById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id);
    }

    @GetMapping("/overdue")
    public List<MaintenanceSchedule> getOverdue() {
        return scheduleService.getOverdueSchedules();
    }

    @GetMapping("/upcoming")
    public List<MaintenanceSchedule> getUpcoming(@RequestParam(defaultValue = "30") int days) {
        return scheduleService.getUpcomingSchedules(days);
    }

    @PatchMapping("/{id}/assign")
    public MaintenanceSchedule assign(@PathVariable Long id, @RequestParam Long technicianId) {
        return scheduleService.assignTechnician(id, technicianId);
    }

    @PostMapping("/{id}/complete")
    public MaintenanceSchedule complete(@PathVariable Long id) {
        return scheduleService.completeSchedule(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        scheduleService.deactivateSchedule(id);
    }
}