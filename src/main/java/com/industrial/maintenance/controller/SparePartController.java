package com.industrial.maintenance.controller;

import com.industrial.maintenance.entity.SparePart;
import com.industrial.maintenance.service.SparePartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Spare Parts", description = "Spare parts catalogue")
@RestController
@RequestMapping("/api/spare-parts")
public class SparePartController {

    private final SparePartService sparePartService;

    public SparePartController(SparePartService sparePartService) {
        this.sparePartService = sparePartService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SparePart create(@Valid @RequestBody SparePart part) {
        return sparePartService.createPart(part);
    }

    @GetMapping
    public List<SparePart> getAll() {
        return sparePartService.getAllParts();
    }

    @GetMapping("/{id}")
    public SparePart getById(@PathVariable Long id) {
        return sparePartService.getPartById(id);
    }
}