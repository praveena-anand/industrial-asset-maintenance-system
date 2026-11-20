package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.SparePartRequest;
import com.industrial.maintenance.dto.SparePartResponse;
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
    public SparePartResponse create(@Valid @RequestBody SparePartRequest request) {
        return SparePartResponse.from(sparePartService.createPart(request));
    }

    @GetMapping
    public List<SparePartResponse> getAll() {
        return sparePartService.getAllParts().stream()
                .map(SparePartResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SparePartResponse getById(@PathVariable Long id) {
        return SparePartResponse.from(sparePartService.getPartById(id));
    }
}