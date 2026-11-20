package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.EquipmentRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipment", description = "Register, view, update, search and retire equipment")
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @Operation(summary = "Register new equipment",
            description = "Equipment code and serial number must be unique, and the factory must exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Factory not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate equipment code or serial number")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment register(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.registerEquipment(request);
    }

    @Operation(summary = "List all equipment")
    @GetMapping
    public List<Equipment> getAll() {
        return equipmentService.getAllEquipment();
    }

    @Operation(summary = "Get equipment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    @GetMapping("/{id}")
    public Equipment getById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id);
    }

    @Operation(summary = "Get equipment by equipment code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    @GetMapping("/code/{code}")
    public Equipment getByCode(@PathVariable String code) {
        return equipmentService.getByEquipmentCode(code);
    }

    @Operation(summary = "Get equipment by serial number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    @GetMapping("/serial/{serialNumber}")
    public Equipment getBySerial(@PathVariable String serialNumber) {
        return equipmentService.getBySerialNumber(serialNumber);
    }

    @Operation(summary = "Update equipment details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Equipment or factory not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate equipment code or serial number")
    })
    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.updateEquipment(id, request);
    }

    @Operation(summary = "Retire equipment",
            description = "Soft delete: sets the status to RETIRED and keeps the record and its history.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipment retired"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retire(@PathVariable Long id) {
        equipmentService.retireEquipment(id);
    }
}