package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.MaintenanceRecordRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.repository.MaintenanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRecordServiceTest {

    @Mock private MaintenanceRecordRepository recordRepository;
    @Mock private EquipmentService equipmentService;
    @Mock private EmployeeService employeeService;
    @Mock private ServiceRequestService serviceRequestService;
    @Mock private MaintenanceScheduleService scheduleService;
    @Mock private SparePartService sparePartService;

    @InjectMocks
    private MaintenanceRecordService recordService;

    private ServiceRequest serviceRequest;

    @BeforeEach
    void setUp() {
        Equipment equipment = new Equipment();
        equipment.setId(1L);

        serviceRequest = new ServiceRequest();
        serviceRequest.setId(10L);
        serviceRequest.setEquipment(equipment);
        serviceRequest.setStatus(ServiceRequestStatus.COMPLETED);

        when(equipmentService.getEquipmentById(1L)).thenReturn(equipment);
        when(employeeService.getActiveTechnician(2L)).thenReturn(new Employee());
        when(serviceRequestService.getRequestById(10L)).thenReturn(serviceRequest);
    }

    private MaintenanceRecordRequest recordForServiceRequest10() {
        return new MaintenanceRecordRequest(1L, 2L, 10L, null, "Replaced thermostat",
                LocalDate.of(2026, 11, 20), new BigDecimal("1000.00"), List.of());
    }

    @Test
    void createRecord_whenServiceRequestAlreadyRecorded_isRejected() {
        when(recordRepository.existsByServiceRequestId(10L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> recordService.createRecord(recordForServiceRequest10()));

        assertEquals("Service request 10 already has a maintenance record", ex.getMessage());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void createRecord_whenServiceRequestNotCompleted_isRejected() {
        serviceRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> recordService.createRecord(recordForServiceRequest10()));

        assertEquals("Service request 10 must be COMPLETED before its work is recorded", ex.getMessage());
        verify(recordRepository, never()).save(any());
    }
}