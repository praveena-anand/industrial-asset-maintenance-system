package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.SensorReadingRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.entity.SensorReading;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.SensorReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorReadingService {

    private static final Logger log = LoggerFactory.getLogger(SensorReadingService.class);

    private final SensorReadingRepository readingRepository;
    private final EquipmentService equipmentService;
    private final AlertService alertService;

    public SensorReadingService(SensorReadingRepository readingRepository,
                                EquipmentService equipmentService,
                                AlertService alertService) {
        this.readingRepository = readingRepository;
        this.equipmentService = equipmentService;
        this.alertService = alertService;
    }

    @Transactional
    public SensorReading recordReading(Long equipmentId, SensorReadingRequest request, Long submittedById) {
        Equipment equipment = equipmentService.getEquipmentById(equipmentId);

        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new BusinessRuleException("Equipment " + equipment.getEquipmentCode()
                    + " is retired and does not accept sensor readings");
        }

        readingRepository.findFirstByEquipmentIdOrderByRecordedAtDesc(equipmentId).ifPresent(latest -> {
            if (request.operatingHours() < latest.getOperatingHours()) {
                throw new BusinessRuleException("Operating hours cannot decrease (last reading: "
                        + latest.getOperatingHours() + ")");
            }
        });

        SensorReading reading = new SensorReading();
        reading.setEquipment(equipment);
        reading.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : LocalDateTime.now());
        reading.setTemperatureCelsius(request.temperatureCelsius());
        reading.setVibrationMmPerSec(request.vibrationMmPerSec());
        reading.setOilPressureBar(request.oilPressureBar());
        reading.setOperatingHours(request.operatingHours());

        SensorReading saved = readingRepository.save(reading);
        log.debug("Recorded reading for {}: temp={} vib={} oil={} hours={}",
                equipment.getEquipmentCode(), saved.getTemperatureCelsius(),
                saved.getVibrationMmPerSec(), saved.getOilPressureBar(), saved.getOperatingHours());

        alertService.evaluate(saved, submittedById);
        return saved;
    }

    public SensorReading getLatestReading(Long equipmentId) {
        Equipment equipment = equipmentService.getEquipmentById(equipmentId);
        return readingRepository.findFirstByEquipmentIdOrderByRecordedAtDesc(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No sensor readings found for equipment " + equipment.getEquipmentCode()));
    }

    public List<SensorReading> getRecentReadings(Long equipmentId) {
        equipmentService.getEquipmentById(equipmentId);
        return readingRepository.findTop20ByEquipmentIdOrderByRecordedAtDesc(equipmentId);
    }
}