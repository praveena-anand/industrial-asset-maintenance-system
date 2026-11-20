package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.FactoryRequest;
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

    public Factory createFactory(FactoryRequest request) {
        if (factoryRepository.existsByFactoryCode(request.factoryCode())) {
            throw new DuplicateResourceException(
                    "Factory code " + request.factoryCode() + " already exists");
        }

        Factory factory = new Factory();          // always a brand-new object, so no ID from the client
        factory.setFactoryCode(request.factoryCode());
        factory.setName(request.name());
        factory.setCity(request.city());
        factory.setAddress(request.address());
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