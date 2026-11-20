package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.EquipmentRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment register(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.registerEquipment(request);
    }

    @GetMapping
    public List<Equipment> getAll() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/{id}")
    public Equipment getById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id);
    }

    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.updateEquipment(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retire(@PathVariable Long id) {
        equipmentService.retireEquipment(id);
    }

    @GetMapping("/code/{code}")
    public Equipment getByCode(@PathVariable String code) {
        return equipmentService.getByEquipmentCode(code);
    }

    @GetMapping("/serial/{serialNumber}")
    public Equipment getBySerial(@PathVariable String serialNumber) {
        return equipmentService.getBySerialNumber(serialNumber);
    }
}