package com.industrial.maintenance.service;

import com.industrial.maintenance.config.SensorThresholds;
import com.industrial.maintenance.dto.EquipmentHealthResponse;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.repository.AlertRepository;
import com.industrial.maintenance.repository.MaintenanceScheduleRepository;
import com.industrial.maintenance.repository.SensorReadingRepository;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentHealthServiceTest {

    @Mock private EquipmentService equipmentService;
    @Mock private SensorReadingRepository readingRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private MaintenanceScheduleRepository scheduleRepository;
    @Mock private ServiceRequestRepository serviceRequestRepository;

    private EquipmentHealthService healthService;

    @BeforeEach
    void setUp() {
        SensorThresholds thresholds = new SensorThresholds(100.0, 110.0, 7.1, 11.2, 3.0, 2.0);
        healthService = new EquipmentHealthService(equipmentService, readingRepository,
                alertRepository, scheduleRepository, serviceRequestRepository, thresholds);

        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setEquipmentCode("EQ1001");
        equipment.setStatus(EquipmentStatus.OPERATIONAL);
        when(equipmentService.getEquipmentById(1L)).thenReturn(equipment);
    }

    private void latestReading(double temperature, double vibration, double oilPressure) {
        SensorReading reading = new SensorReading();
        reading.setTemperatureCelsius(temperature);
        reading.setVibrationMmPerSec(vibration);
        reading.setOilPressureBar(oilPressure);
        reading.setRecordedAt(LocalDateTime.now());
        when(readingRepository.findFirstByEquipmentIdOrderByRecordedAtDesc(1L)).thenReturn(Optional.of(reading));
    }

    private void counts(long criticalAlerts, long overdueTasks, long recentBreakdowns) {
        when(alertRepository.countByEquipmentIdAndSeverityAndStatusIn(eq(1L), eq(AlertSeverity.CRITICAL), anyList()))
                .thenReturn(criticalAlerts);
        when(scheduleRepository.countByEquipmentIdAndActiveTrueAndNextDueDateBefore(eq(1L), any(LocalDate.class)))
                .thenReturn(overdueTasks);
        when(serviceRequestRepository.countByEquipmentIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(recentBreakdowns);
    }

    @Test
    void healthyMachine_scores100_andIsGood() {
        latestReading(80.0, 3.0, 4.2);
        counts(0, 0, 0);

        EquipmentHealthResponse health = healthService.getHealth(1L);

        assertEquals(100, health.score());
        assertEquals(HealthStatus.GOOD, health.status());
        assertEquals("No issues detected", health.factors().get(0));
    }

    @Test
    void troubledMachine_hasDeductionsExplained_andIsAtRisk() {
        latestReading(115.0, 3.0, 4.2);   // critical temperature: -30
        counts(0, 1, 3);                  // 1 overdue task: -10, 3 breakdowns: -15

        EquipmentHealthResponse health = healthService.getHealth(1L);

        assertEquals(45, health.score());   // 100 - 30 - 10 - 15
        assertEquals(HealthStatus.AT_RISK, health.status());
        assertEquals(3, health.factors().size());
    }

    @Test
    void warningLevelReading_givesFairScore() {
        latestReading(80.0, 8.0, 2.5);    // vibration warning: -15, oil pressure warning: -15
        counts(0, 0, 0);

        EquipmentHealthResponse health = healthService.getHealth(1L);

        assertEquals(70, health.score());
        assertEquals(HealthStatus.FAIR, health.status());
    }

    @Test
    void penaltiesAreCapped_soOneFactorCannotDominate() {
        latestReading(80.0, 3.0, 4.2);
        counts(0, 0, 10);                 // 10 breakdowns would be -50, but the cap is -20

        EquipmentHealthResponse health = healthService.getHealth(1L);

        assertEquals(80, health.score());
    }

    @Test
    void machineWithoutReadings_isNotPenalised_butSaysSo() {
        when(readingRepository.findFirstByEquipmentIdOrderByRecordedAtDesc(1L)).thenReturn(Optional.empty());
        counts(0, 0, 0);

        EquipmentHealthResponse health = healthService.getHealth(1L);

        assertEquals(100, health.score());
        assertNull(health.lastReadingAt());
        assertTrue(health.factors().contains("No sensor readings available"));
    }
}