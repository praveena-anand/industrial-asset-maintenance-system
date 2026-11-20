package com.industrial.maintenance.controller;

import com.industrial.maintenance.entity.Factory;
import com.industrial.maintenance.service.FactoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factories")
public class FactoryController {

    private final FactoryService factoryService;

    public FactoryController(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Factory create(@Valid @RequestBody Factory factory) {
        return factoryService.createFactory(factory);
    }

    @GetMapping
    public List<Factory> getAll() {
        return factoryService.getAllFactories();
    }

    @GetMapping("/{id}")
    public Factory getById(@PathVariable Long id) {
        return factoryService.getFactoryById(id);
    }
}