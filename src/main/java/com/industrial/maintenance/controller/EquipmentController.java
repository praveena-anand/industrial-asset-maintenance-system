package com.industrial.maintenance.controller;

import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public Equipment register(@RequestBody Equipment equipment) {
        return equipmentService.registerEquipment(equipment);
    }

    @GetMapping
    public List<Equipment> getAll() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/{id}")
    public Equipment getById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id);
    }
}