package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.EquipmentRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.entity.Factory;
import com.industrial.maintenance.exception.DuplicateEquipmentException;
import com.industrial.maintenance.exception.EquipmentNotFoundException;
import com.industrial.maintenance.repository.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private final EquipmentRepository equipmentRepository;
    private final FactoryService factoryService;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            FactoryService factoryService) {
        this.equipmentRepository = equipmentRepository;
        this.factoryService = factoryService;
    }

    public Equipment registerEquipment(EquipmentRequest request) {
        log.info("Registering equipment {} (serial {})", request.equipmentCode(), request.serialNumber());

        if (equipmentRepository.existsByEquipmentCode(request.equipmentCode())) {
            log.warn("Rejected equipment registration: code {} already exists", request.equipmentCode());
            throw new DuplicateEquipmentException(
                    "Equipment code " + request.equipmentCode() + " already exists");
        }
        if (equipmentRepository.existsBySerialNumber(request.serialNumber())) {
            log.warn("Rejected equipment registration: serial {} already exists", request.serialNumber());
            throw new DuplicateEquipmentException(
                    "Serial number " + request.serialNumber() + " already exists");
        }

        Equipment equipment = new Equipment();
        applyRequest(equipment, request);
        if (equipment.getStatus() == null) {
            equipment.setStatus(EquipmentStatus.OPERATIONAL);
        }
        Equipment saved = equipmentRepository.save(equipment);
        log.info("Registered equipment {} with ID {}", saved.getEquipmentCode(), saved.getId());
        return saved;
    }

    public List<Equipment> getAllEquipment() {
        log.debug("Fetching all equipment");
        return equipmentRepository.findAll();
    }

    public Equipment getEquipmentById(Long id) {
        log.debug("Fetching equipment with ID {}", id);
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(
                        "Equipment with ID " + id + " not found"));
    }

    public Equipment getByEquipmentCode(String code) {
        log.debug("Fetching equipment with code {}", code);
        return equipmentRepository.findByEquipmentCode(code)
                .orElseThrow(() -> new EquipmentNotFoundException(
                        "Equipment with code " + code + " not found"));
    }

    public Equipment getBySerialNumber(String serialNumber) {
        log.debug("Fetching equipment with serial {}", serialNumber);
        return equipmentRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new EquipmentNotFoundException(
                        "Equipment with serial number " + serialNumber + " not found"));
    }

    public Equipment updateEquipment(Long id, EquipmentRequest request) {
        Equipment existing = getEquipmentById(id);

        if (equipmentRepository.existsByEquipmentCodeAndIdNot(request.equipmentCode(), id)) {
            log.warn("Rejected update of equipment {}: code {} already exists", id, request.equipmentCode());
            throw new DuplicateEquipmentException(
                    "Equipment code " + request.equipmentCode() + " already exists");
        }
        if (equipmentRepository.existsBySerialNumberAndIdNot(request.serialNumber(), id)) {
            log.warn("Rejected update of equipment {}: serial {} already exists", id, request.serialNumber());
            throw new DuplicateEquipmentException(
                    "Serial number " + request.serialNumber() + " already exists");
        }

        applyRequest(existing, request);
        Equipment saved = equipmentRepository.save(existing);
        log.info("Updated equipment {} (ID {})", saved.getEquipmentCode(), id);
        return saved;
    }

    public void retireEquipment(Long id) {
        Equipment equipment = getEquipmentById(id);
        equipment.setStatus(EquipmentStatus.RETIRED);
        equipmentRepository.save(equipment);
        log.info("Retired equipment {} (ID {})", equipment.getEquipmentCode(), id);
    }

    public void updateStatus(Equipment equipment, EquipmentStatus status) {
        EquipmentStatus previous = equipment.getStatus();
        equipment.setStatus(status);
        equipmentRepository.save(equipment);
        log.info("Equipment {} status changed: {} -> {}", equipment.getEquipmentCode(), previous, status);
    }

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