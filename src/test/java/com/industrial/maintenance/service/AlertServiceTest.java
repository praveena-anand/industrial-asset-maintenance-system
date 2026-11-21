package com.industrial.maintenance.service;

import com.industrial.maintenance.config.SensorThresholds;
import com.industrial.maintenance.dto.ServiceRequestRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock private AlertRepository alertRepository;
    @Mock private ServiceRequestService serviceRequestService;
    @Mock private EquipmentService equipmentService;
    @Mock private EmployeeService employeeService;

    private AlertService alertService;
    private Equipment equipment;

    private static final Long SENSOR_ID = 6L;

    @BeforeEach
    void setUp() {
        // Real threshold values: warning/critical temperature 100/110, vibration 7.1/11.2, oil pressure 3.0/2.0
        SensorThresholds thresholds = new SensorThresholds(100.0, 110.0, 7.1, 11.2, 3.0, 2.0);
        alertService = new AlertService(alertRepository, serviceRequestService,
                equipmentService, employeeService, thresholds);

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setEquipmentCode("EQ1001");
    }

    private SensorReading reading(double temperature, double vibration, double oilPressure) {
        SensorReading reading = new SensorReading();
        reading.setEquipment(equipment);
        reading.setTemperatureCelsius(temperature);
        reading.setVibrationMmPerSec(vibration);
        reading.setOilPressureBar(oilPressure);
        reading.setOperatingHours(1000.0);
        return reading;
    }

    private void noExistingAlert(AlertType type) {
        when(alertRepository.findFirstByEquipmentIdAndTypeAndStatusIn(eq(1L), eq(type), anyList()))
                .thenReturn(Optional.empty());
    }

    @Test
    void normalReading_raisesNoAlert() {
        alertService.evaluate(reading(80.0, 3.0, 4.2), SENSOR_ID);

        verify(alertRepository, never()).save(any());
        verify(serviceRequestService, never()).createRequest(any());
    }

    @Test
    void temperatureAboveWarning_raisesWarningWithoutServiceRequest() {
        noExistingAlert(AlertType.HIGH_TEMPERATURE);

        alertService.evaluate(reading(104.0, 3.0, 4.2), SENSOR_ID);

        ArgumentCaptor<Alert> saved = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(saved.capture());
        assertEquals(AlertType.HIGH_TEMPERATURE, saved.getValue().getType());
        assertEquals(AlertSeverity.WARNING, saved.getValue().getSeverity());
        assertEquals(AlertStatus.OPEN, saved.getValue().getStatus());
        assertEquals(100.0, saved.getValue().getThresholdValue());
        assertNull(saved.getValue().getServiceRequest());
        verify(serviceRequestService, never()).createRequest(any());
    }

    @Test
    void temperatureAboveCritical_raisesCriticalAlertAndCreatesServiceRequest() {
        noExistingAlert(AlertType.HIGH_TEMPERATURE);
        ServiceRequest created = new ServiceRequest();
        created.setId(50L);
        when(serviceRequestService.createRequest(any(ServiceRequestRequest.class))).thenReturn(created);

        alertService.evaluate(reading(115.0, 3.0, 4.2), SENSOR_ID);

        ArgumentCaptor<Alert> savedAlert = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(savedAlert.capture());
        assertEquals(AlertSeverity.CRITICAL, savedAlert.getValue().getSeverity());
        assertEquals(created, savedAlert.getValue().getServiceRequest());

        ArgumentCaptor<ServiceRequestRequest> request = ArgumentCaptor.forClass(ServiceRequestRequest.class);
        verify(serviceRequestService).createRequest(request.capture());
        assertEquals(Priority.CRITICAL, request.getValue().priority());
        assertEquals(SENSOR_ID, request.getValue().reportedById());
        assertTrue(request.getValue().problemDescription().startsWith("[Automatic]"));
    }

    @Test
    void existingWarning_isEscalatedToCritical_notDuplicated() {
        Alert existing = new Alert();
        existing.setEquipment(equipment);
        existing.setType(AlertType.HIGH_TEMPERATURE);
        existing.setSeverity(AlertSeverity.WARNING);
        existing.setStatus(AlertStatus.OPEN);
        when(alertRepository.findFirstByEquipmentIdAndTypeAndStatusIn(eq(1L), eq(AlertType.HIGH_TEMPERATURE), anyList()))
                .thenReturn(Optional.of(existing));
        when(serviceRequestService.createRequest(any(ServiceRequestRequest.class))).thenReturn(new ServiceRequest());

        alertService.evaluate(reading(112.0, 3.0, 4.2), SENSOR_ID);

        assertEquals(AlertSeverity.CRITICAL, existing.getSeverity());
        assertEquals(112.0, existing.getTriggerValue());
        assertNotNull(existing.getServiceRequest());
        verify(alertRepository, times(1)).save(existing);   // the SAME alert, updated
    }

    @Test
    void existingCriticalAlert_preventsDuplicates() {
        Alert existing = new Alert();
        existing.setEquipment(equipment);
        existing.setSeverity(AlertSeverity.CRITICAL);
        existing.setStatus(AlertStatus.OPEN);
        when(alertRepository.findFirstByEquipmentIdAndTypeAndStatusIn(eq(1L), eq(AlertType.HIGH_TEMPERATURE), anyList()))
                .thenReturn(Optional.of(existing));

        alertService.evaluate(reading(120.0, 3.0, 4.2), SENSOR_ID);

        verify(alertRepository, never()).save(any());
        verify(serviceRequestService, never()).createRequest(any());
    }

    @Test
    void lowOilPressure_raisesAlert_andWithoutReporterNoServiceRequestIsCreated() {
        noExistingAlert(AlertType.LOW_OIL_PRESSURE);

        alertService.evaluate(reading(80.0, 3.0, 1.5), null);   // critical: below 2.0 bar

        ArgumentCaptor<Alert> saved = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(saved.capture());
        assertEquals(AlertType.LOW_OIL_PRESSURE, saved.getValue().getType());
        assertEquals(AlertSeverity.CRITICAL, saved.getValue().getSeverity());
        assertNull(saved.getValue().getServiceRequest());
        verify(serviceRequestService, never()).createRequest(any());
    }

    @Test
    void acknowledge_onAlreadyAcknowledgedAlert_isRejected() {
        Alert alert = new Alert();
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        when(alertRepository.findById(7L)).thenReturn(Optional.of(alert));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> alertService.acknowledge(7L, 2L));

        assertTrue(ex.getMessage().startsWith("Only OPEN alerts can be acknowledged"));
        verify(alertRepository, never()).save(any());
    }
}