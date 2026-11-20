package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.SparePartRequest;
import com.industrial.maintenance.entity.SparePart;
import com.industrial.maintenance.exception.DuplicateResourceException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.SparePartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SparePartService {

    private final SparePartRepository sparePartRepository;

    public SparePartService(SparePartRepository sparePartRepository) {
        this.sparePartRepository = sparePartRepository;
    }

    public SparePart createPart(SparePartRequest request) {
        if (sparePartRepository.existsByPartNumber(request.partNumber())) {
            throw new DuplicateResourceException(
                    "Part number " + request.partNumber() + " already exists");
        }

        SparePart part = new SparePart();
        part.setPartNumber(request.partNumber());
        part.setName(request.name());
        part.setUnitPrice(request.unitPrice());
        return sparePartRepository.save(part);
    }

    public List<SparePart> getAllParts() {
        return sparePartRepository.findAll();
    }

    public SparePart getPartById(Long id) {
        return sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Spare part with ID " + id + " not found"));
    }
}