package com.industrial.maintenance.service;

import com.industrial.maintenance.entity.Factory;
import com.industrial.maintenance.exception.DuplicateResourceException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.FactoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactoryService {

    private final FactoryRepository factoryRepository;

    public FactoryService(FactoryRepository factoryRepository) {
        this.factoryRepository = factoryRepository;
    }

    public Factory createFactory(Factory factory) {
        if (factoryRepository.existsByFactoryCode(factory.getFactoryCode())) {
            throw new DuplicateResourceException(
                    "Factory code " + factory.getFactoryCode() + " already exists");
        }
        return factoryRepository.save(factory);
    }

    public List<Factory> getAllFactories() {
        return factoryRepository.findAll();
    }

    public Factory getFactoryById(Long id) {
        return factoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factory with ID " + id + " not found"));
    }
}