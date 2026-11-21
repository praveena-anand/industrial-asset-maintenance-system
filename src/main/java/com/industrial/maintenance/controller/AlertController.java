package com.industrial.maintenance.controller;

import com.industrial.maintenance.config.JwtClaims;
import com.industrial.maintenance.dto.AlertResponse;
import com.industrial.maintenance.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alerts", description = "Sensor-based equipment alerts")
@RestController
@RequestMapping("/api")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @Operation(summary = "List all unresolved alerts (OPEN and ACKNOWLEDGED), newest first")
    @GetMapping("/alerts")
    public List<AlertResponse> getUnresolved() {
        return alertService.getUnresolvedAlerts().stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Operation(summary = "Get an alert by ID")
    @GetMapping("/alerts/{id}")
    public AlertResponse getById(@PathVariable Long id) {
        return AlertResponse.from(alertService.getAlertById(id));
    }

    @Operation(summary = "List all alerts for a machine, including resolved ones")
    @GetMapping("/equipment/{equipmentId}/alerts")
    public List<AlertResponse> getForEquipment(@PathVariable Long equipmentId) {
        return alertService.getAlertsForEquipment(equipmentId).stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Operation(summary = "Acknowledge an OPEN alert")
    @PatchMapping("/alerts/{id}/acknowledge")
    public AlertResponse acknowledge(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return AlertResponse.from(alertService.acknowledge(id, JwtClaims.employeeId(jwt)));
    }

    @Operation(summary = "Mark an alert as resolved")
    @PatchMapping("/alerts/{id}/resolve")
    public AlertResponse resolve(@PathVariable Long id) {
        return AlertResponse.from(alertService.resolve(id));
    }
}