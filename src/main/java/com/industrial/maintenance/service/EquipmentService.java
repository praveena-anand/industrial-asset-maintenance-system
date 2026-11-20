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

    public Equipment updateEquipment(Long id, Equipment updated) {
        Equipment existing = getEquipmentById(id);

        if (equipmentRepository.existsByEquipmentCodeAndIdNot(updated.getEquipmentCode(), id)) {
            throw new DuplicateEquipmentException(
                    "Equipment code " + updated.getEquipmentCode() + " already exists");
        }
        if (equipmentRepository.existsBySerialNumberAndIdNot(updated.getSerialNumber(), id)) {
            throw new DuplicateEquipmentException(
                    "Serial number " + updated.getSerialNumber() + " already exists");
        }

        existing.setEquipmentCode(updated.getEquipmentCode());
        existing.setName(updated.getName());
        existing.setModel(updated.getModel());
        existing.setSerialNumber(updated.getSerialNumber());
        existing.setLocation(updated.getLocation());
        existing.setPurchaseDate(updated.getPurchaseDate());
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        return equipmentRepository.save(existing);
    }

    public void retireEquipment(Long id) {
        Equipment equipment = getEquipmentById(id);
        equipment.setStatus(EquipmentStatus.RETIRED);
        equipmentRepository.save(equipment);
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
}