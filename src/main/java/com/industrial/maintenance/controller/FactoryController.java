package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.FactoryRequest;
import com.industrial.maintenance.dto.FactoryResponse;
import com.industrial.maintenance.service.FactoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Factories", description = "Manage factory locations")
@RestController
@RequestMapping("/api/factories")
public class FactoryController {

    private final FactoryService factoryService;

    public FactoryController(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FactoryResponse create(@Valid @RequestBody FactoryRequest request) {
        return FactoryResponse.from(factoryService.createFactory(request));
    }

    @GetMapping
    public List<FactoryResponse> getAll() {
        return factoryService.getAllFactories().stream()
                .map(FactoryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public FactoryResponse getById(@PathVariable Long id) {
        return FactoryResponse.from(factoryService.getFactoryById(id));
    }
}