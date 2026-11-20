package com.industrial.maintenance.service;

import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.exception.DuplicateEquipmentException;
import com.industrial.maintenance.exception.EquipmentNotFoundException;
import com.industrial.maintenance.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public Equipment registerEquipment(Equipment equipment) {
        if (equipmentRepository.existsByEquipmentCode(equipment.getEquipmentCode())) {
            throw new DuplicateEquipmentException(
                    "Equipment code " + equipment.getEquipmentCode() + " already exists");
        }
        if (equipmentRepository.existsBySerialNumber(equipment.getSerialNumber())) {
            throw new DuplicateEquipmentException(
                    "Serial number " + equipment.getSerialNumber() + " already exists");
        }
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
}