package com.industrial.maintenance.service;

import com.industrial.maintenance.config.SensorThresholds;
import com.industrial.maintenance.dto.ServiceRequestRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    static final List<AlertStatus> UNRESOLVED = List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED);

    private final AlertRepository alertRepository;
    private final ServiceRequestService serviceRequestService;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;
    private final SensorThresholds thresholds;

    public AlertService(AlertRepository alertRepository,
                        ServiceRequestService serviceRequestService,
                        EquipmentService equipmentService,
                        EmployeeService employeeService,
                        SensorThresholds thresholds) {
        this.alertRepository = alertRepository;
        this.serviceRequestService = serviceRequestService;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
        this.thresholds = thresholds;
    }

    // ---------- Evaluating readings ----------

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

        AlertSeverity severity;
        double limit;
        if (crosses(value, criticalLimit, higherIsWorse)) {
            severity = AlertSeverity.CRITICAL;
            limit = criticalLimit;
        } else if (crosses(value, warningLimit, higherIsWorse)) {
            severity = AlertSeverity.WARNING;
            limit = warningLimit;
        } else {
            return;
        }

        Equipment equipment = reading.getEquipment();
        String message = messageFormat.formatted(value, limit);

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
            return;
        }

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

    // ---------- Viewing and handling alerts ----------

    public List<Alert> getUnresolvedAlerts() {
        return alertRepository.findByStatusInOrderByCreatedAtDesc(UNRESOLVED);
    }

    public List<Alert> getAlertsForEquipment(Long equipmentId) {
        equipmentService.getEquipmentById(equipmentId);   // 404 if the machine doesn't exist
        return alertRepository.findByEquipmentIdOrderByCreatedAtDesc(equipmentId);
    }

    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with ID " + id + " not found"));
    }

    public Alert acknowledge(Long id, Long employeeId) {
        Alert alert = getAlertById(id);
        if (alert.getStatus() != AlertStatus.OPEN) {
            throw new BusinessRuleException("Only OPEN alerts can be acknowledged (alert "
                    + id + " is " + alert.getStatus() + ")");
        }
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        if (employeeId != null) {
            alert.setAcknowledgedBy(employeeService.getEmployeeById(employeeId));
        }
        Alert saved = alertRepository.save(alert);
        log.info("Alert {} acknowledged by {}", id,
                saved.getAcknowledgedBy() != null ? saved.getAcknowledgedBy().getEmployeeCode() : "unknown");
        return saved;
    }

    public Alert resolve(Long id) {
        Alert alert = getAlertById(id);
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new BusinessRuleException("Alert " + id + " is already resolved");
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);
        log.info("Alert {} on {} resolved", id, alert.getEquipment().getEquipmentCode());
        return saved;
    }
}