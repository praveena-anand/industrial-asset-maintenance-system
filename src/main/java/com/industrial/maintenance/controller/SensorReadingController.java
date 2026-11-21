package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.SensorReadingRequest;
import com.industrial.maintenance.dto.SensorReadingResponse;
import com.industrial.maintenance.service.SensorReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sensor Readings", description = "Submit and view equipment sensor data")
@RestController
@RequestMapping("/api/equipment/{equipmentId}/readings")
public class SensorReadingController {

    private final SensorReadingService readingService;

    public SensorReadingController(SensorReadingService readingService) {
        this.readingService = readingService;
    }

    @Operation(summary = "Submit a sensor reading (SENSOR or ADMIN role)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorReadingResponse submit(@PathVariable Long equipmentId,
                                        @Valid @RequestBody SensorReadingRequest request) {
        return SensorReadingResponse.from(readingService.recordReading(equipmentId, request));
    }

    @Operation(summary = "Get the latest reading for a machine")
    @GetMapping("/latest")
    public SensorReadingResponse latest(@PathVariable Long equipmentId) {
        return SensorReadingResponse.from(readingService.getLatestReading(equipmentId));
    }

    @Operation(summary = "Get the 20 most recent readings for a machine")
    @GetMapping
    public List<SensorReadingResponse> recent(@PathVariable Long equipmentId) {
        return readingService.getRecentReadings(equipmentId).stream()
                .map(SensorReadingResponse::from)
                .toList();
    }
}