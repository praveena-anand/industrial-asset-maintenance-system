package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.EquipmentRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.entity.Factory;
import com.industrial.maintenance.exception.DuplicateEquipmentException;
import com.industrial.maintenance.exception.EquipmentNotFoundException;
import com.industrial.maintenance.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final FactoryService factoryService;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            FactoryService factoryService) {
        this.equipmentRepository = equipmentRepository;
        this.factoryService = factoryService;
    }

    public Equipment registerEquipment(EquipmentRequest request) {
        if (equipmentRepository.existsByEquipmentCode(request.equipmentCode())) {
            throw new DuplicateEquipmentException(
                    "Equipment code " + request.equipmentCode() + " already exists");
        }
        if (equipmentRepository.existsBySerialNumber(request.serialNumber())) {
            throw new DuplicateEquipmentException(
                    "Serial number " + request.serialNumber() + " already exists");
        }

        Equipment equipment = new Equipment();
        applyRequest(equipment, request);
        if (equipment.getStatus() == null) {
            equipment.setStatus(EquipmentStatus.OPERATIONAL);
        }
        return equipmentRepository.save(equipment);
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(
                        "Equipment with ID " + id + " not found"));
    }

    public Equipment getByEquipmentCode(String code) {
        return equipmentRepository.findByEquipmentCode(code)
                .orElseThrow(() -> new EquipmentNotFoundException(
                        "Equipment with code " + code + " not found"));
    }

    public Equipment getBySerialNumber(String serialNumber) {
        return equipmentRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new EquipmentNotFoundException(
                        "Equipment with serial number " + serialNumber + " not found"));
    }

    public Equipment updateEquipment(Long id, EquipmentRequest request) {
        Equipment existing = getEquipmentById(id);

        if (equipmentRepository.existsByEquipmentCodeAndIdNot(request.equipmentCode(), id)) {
            throw new DuplicateEquipmentException(
                    "Equipment code " + request.equipmentCode() + " already exists");
        }
        if (equipmentRepository.existsBySerialNumberAndIdNot(request.serialNumber(), id)) {
            throw new DuplicateEquipmentException(
                    "Serial number " + request.serialNumber() + " already exists");
        }

        applyRequest(existing, request);
        return equipmentRepository.save(existing);
    }

    public void updateStatus(Equipment equipment, EquipmentStatus status) {
        equipment.setStatus(status);
        equipmentRepository.save(equipment);
    }

    public void retireEquipment(Long id) {
        Equipment equipment = getEquipmentById(id);
        equipment.setStatus(EquipmentStatus.RETIRED);
        equipmentRepository.save(equipment);
    }

    // Copies values from the request onto the entity
    private void applyRequest(Equipment equipment, EquipmentRequest request) {
        Factory factory = factoryService.getFactoryById(request.factoryId());

        equipment.setEquipmentCode(request.equipmentCode());
        equipment.setName(request.name());
        equipment.setModel(request.model());
        equipment.setSerialNumber(request.serialNumber());
        equipment.setPurchaseDate(request.purchaseDate());
        equipment.setFactory(factory);
        if (request.status() != null) {
            equipment.setStatus(request.status());
        }
    }
}