package com.industrial.maintenance.service;

import com.industrial.maintenance.config.SensorThresholds;
import com.industrial.maintenance.dto.EquipmentHealthResponse;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.repository.AlertRepository;
import com.industrial.maintenance.repository.MaintenanceScheduleRepository;
import com.industrial.maintenance.repository.SensorReadingRepository;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EquipmentHealthService {

    private static final int WARNING_PENALTY = 15;
    private static final int CRITICAL_PENALTY = 30;
    private static final int PER_CRITICAL_ALERT = 10;
    private static final int PER_OVERDUE_TASK = 10;
    private static final int PER_RECENT_BREAKDOWN = 5;
    private static final int MAX_PER_FACTOR = 20;
    private static final int BREAKDOWN_WINDOW_DAYS = 90;

    private final EquipmentService equipmentService;
    private final SensorReadingRepository readingRepository;
    private final AlertRepository alertRepository;
    private final MaintenanceScheduleRepository scheduleRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SensorThresholds thresholds;

    public EquipmentHealthService(EquipmentService equipmentService,
                                  SensorReadingRepository readingRepository,
                                  AlertRepository alertRepository,
                                  MaintenanceScheduleRepository scheduleRepository,
                                  ServiceRequestRepository serviceRequestRepository,
                                  SensorThresholds thresholds) {
        this.equipmentService = equipmentService;
        this.readingRepository = readingRepository;
        this.alertRepository = alertRepository;
        this.scheduleRepository = scheduleRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.thresholds = thresholds;
    }

    @Transactional(readOnly = true)
    public EquipmentHealthResponse getHealth(Long equipmentId) {
        return calculate(equipmentService.getEquipmentById(equipmentId));
    }

    /** Health of every machine that is not retired, worst first. */
    @Transactional(readOnly = true)
    public List<EquipmentHealthResponse> getHealthOverview() {
        return equipmentService.getAllEquipment().stream()
                .filter(equipment -> equipment.getStatus() != EquipmentStatus.RETIRED)
                .map(this::calculate)
                .sorted(Comparator.comparingInt(EquipmentHealthResponse::score))
                .toList();
    }

    private EquipmentHealthResponse calculate(Equipment equipment) {
        Long id = equipment.getId();
        int score = 100;
        List<String> factors = new ArrayList<>();

        // 1. Current condition, from the latest sensor reading
        Optional<SensorReading> latest = readingRepository.findFirstByEquipmentIdOrderByRecordedAtDesc(id);
        if (latest.isPresent()) {
            SensorReading reading = latest.get();
            score -= sensorPenalty("Temperature", reading.getTemperatureCelsius(), "°C",
                    thresholds.temperatureWarning(), thresholds.temperatureCritical(), true, factors);
            score -= sensorPenalty("Vibration", reading.getVibrationMmPerSec(), "mm/s",
                    thresholds.vibrationWarning(), thresholds.vibrationCritical(), true, factors);
            score -= sensorPenalty("Oil pressure", reading.getOilPressureBar(), "bar",
                    thresholds.oilPressureWarning(), thresholds.oilPressureCritical(), false, factors);
        } else {
            factors.add("No sensor readings available");
        }

        // 2. Unresolved critical alerts
        long criticalAlerts = alertRepository.countByEquipmentIdAndSeverityAndStatusIn(
                id, AlertSeverity.CRITICAL, AlertService.UNRESOLVED);
        score -= countPenalty(criticalAlerts, PER_CRITICAL_ALERT, "unresolved critical alert(s)", factors);

        // 3. Overdue preventive maintenance
        long overdueTasks = scheduleRepository.countByEquipmentIdAndActiveTrueAndNextDueDateBefore(
                id, LocalDate.now());
        score -= countPenalty(overdueTasks, PER_OVERDUE_TASK, "overdue maintenance task(s)", factors);

        // 4. Recent breakdowns
        long recentBreakdowns = serviceRequestRepository.countByEquipmentIdAndCreatedAtAfter(
                id, LocalDateTime.now().minusDays(BREAKDOWN_WINDOW_DAYS));
        score -= countPenalty(recentBreakdowns, PER_RECENT_BREAKDOWN,
                "breakdown(s) in the last " + BREAKDOWN_WINDOW_DAYS + " days", factors);

        score = Math.max(0, Math.min(100, score));
        HealthStatus status = score >= 80 ? HealthStatus.GOOD
                : score >= 50 ? HealthStatus.FAIR
                : HealthStatus.AT_RISK;

        if (factors.isEmpty()) {
            factors.add("No issues detected");
        }

        return new EquipmentHealthResponse(id, equipment.getEquipmentCode(), equipment.getName(),
                score, status, latest.map(SensorReading::getRecordedAt).orElse(null), factors);
    }

    private int sensorPenalty(String name, double value, String unit, double warningLimit,
                              double criticalLimit, boolean higherIsWorse, List<String> factors) {
        boolean critical = higherIsWorse ? value > criticalLimit : value < criticalLimit;
        boolean warning = higherIsWorse ? value > warningLimit : value < warningLimit;

        if (critical) {
            factors.add("%s %.1f %s is beyond the critical limit (-%d)".formatted(name, value, unit, CRITICAL_PENALTY));
            return CRITICAL_PENALTY;
        }
        if (warning) {
            factors.add("%s %.1f %s is beyond the warning limit (-%d)".formatted(name, value, unit, WARNING_PENALTY));
            return WARNING_PENALTY;
        }
        return 0;
    }

    private int countPenalty(long count, int perItem, String description, List<String> factors) {
        if (count == 0) {
            return 0;
        }
        int penalty = (int) Math.min(count * perItem, MAX_PER_FACTOR);
        factors.add("%d %s (-%d)".formatted(count, description, penalty));
        return penalty;
    }
}