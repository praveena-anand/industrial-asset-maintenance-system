package com.industrial.maintenance.service;

import com.industrial.maintenance.config.SensorThresholds;
import com.industrial.maintenance.dto.ServiceRequestRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    static final List<AlertStatus> UNRESOLVED = List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED);

    private final AlertRepository alertRepository;
    private final ServiceRequestService serviceRequestService;
    private final SensorThresholds thresholds;

    public AlertService(AlertRepository alertRepository,
                        ServiceRequestService serviceRequestService,
                        SensorThresholds thresholds) {
        this.alertRepository = alertRepository;
        this.serviceRequestService = serviceRequestService;
        this.thresholds = thresholds;
    }

    /** Checks one reading against every threshold. */
    public void evaluate(SensorReading reading, Long reporterId) {
        check(reading, reporterId, AlertType.HIGH_TEMPERATURE, reading.getTemperatureCelsius(),
                thresholds.temperatureWarning(), thresholds.temperatureCritical(), true,
                "Engine temperature %.1f °C exceeds %.1f °C");

        check(reading, reporterId, AlertType.HIGH_VIBRATION, reading.getVibrationMmPerSec(),
                thresholds.vibrationWarning(), thresholds.vibrationCritical(), true,
                "Vibration %.2f mm/s exceeds %.2f mm/s");

        check(reading, reporterId, AlertType.LOW_OIL_PRESSURE, reading.getOilPressureBar(),
                thresholds.oilPressureWarning(), thresholds.oilPressureCritical(), false,
                "Oil pressure %.2f bar is below %.2f bar");
    }

    private void check(SensorReading reading, Long reporterId, AlertType type, double value,
                       double warningLimit, double criticalLimit, boolean higherIsWorse,
                       String messageFormat) {

        // 1. How bad is this value?
        AlertSeverity severity;
        double limit;
        if (crosses(value, criticalLimit, higherIsWorse)) {
            severity = AlertSeverity.CRITICAL;
            limit = criticalLimit;
        } else if (crosses(value, warningLimit, higherIsWorse)) {
            severity = AlertSeverity.WARNING;
            limit = warningLimit;
        } else {
            return;   // within normal range
        }

        Equipment equipment = reading.getEquipment();
        String message = messageFormat.formatted(value, limit);

        // 2. Is there already an unresolved alert of this type for this machine?
        Optional<Alert> existing = alertRepository.findFirstByEquipmentIdAndTypeAndStatusIn(
                equipment.getId(), type, UNRESOLVED);

        if (existing.isPresent()) {
            Alert alert = existing.get();
            if (alert.getSeverity() == AlertSeverity.WARNING && severity == AlertSeverity.CRITICAL) {
                alert.setSeverity(AlertSeverity.CRITICAL);
                alert.setMessage(message);
                alert.setTriggerValue(value);
                alert.setThresholdValue(limit);
                createServiceRequestIfNeeded(alert, reporterId);
                alertRepository.save(alert);
                log.warn("Alert {} on {} escalated to CRITICAL: {}", alert.getId(), equipment.getEquipmentCode(), message);
            }
            return;   // otherwise: already alerted, don't create a duplicate
        }

        // 3. Create a new alert
        Alert alert = new Alert();
        alert.setEquipment(equipment);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.OPEN);
        alert.setMessage(message);
        alert.setTriggerValue(value);
        alert.setThresholdValue(limit);
        if (severity == AlertSeverity.CRITICAL) {
            createServiceRequestIfNeeded(alert, reporterId);
        }
        alertRepository.save(alert);
        log.warn("{} {} alert raised on {}: {}", severity, type, equipment.getEquipmentCode(), message);
    }

    private boolean crosses(double value, double limit, boolean higherIsWorse) {
        return higherIsWorse ? value > limit : value < limit;
    }

    private void createServiceRequestIfNeeded(Alert alert, Long reporterId) {
        if (alert.getServiceRequest() != null) {
            return;
        }
        if (reporterId == null) {
            log.warn("No reporter known; cannot create a service request for the alert on {}",
                    alert.getEquipment().getEquipmentCode());
            return;
        }
        ServiceRequest request = serviceRequestService.createRequest(new ServiceRequestRequest(
                alert.getEquipment().getId(),
                reporterId,
                "[Automatic] " + alert.getMessage(),
                Priority.CRITICAL));
        alert.setServiceRequest(request);
    }
}