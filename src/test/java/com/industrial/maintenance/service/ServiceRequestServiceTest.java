package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.CompleteServiceRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository requestRepository;

    @Mock
    private EquipmentService equipmentService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    private Equipment equipment;
    private ServiceRequest request;

    @BeforeEach
    void setUp() {
        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setEquipmentCode("EQ1001");
        equipment.setStatus(EquipmentStatus.OPERATIONAL);

        request = new ServiceRequest();
        request.setId(10L);
        request.setEquipment(equipment);
        request.setStatus(ServiceRequestStatus.OPEN);
    }

    @Test
    void assignTechnician_onOpenRequest_movesItToAssigned() {
        Employee technician = new Employee();
        technician.setId(1L);
        technician.setEmployeeCode("EMP1001");
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(employeeService.getActiveTechnician(1L)).thenReturn(technician);
        when(requestRepository.save(request)).thenReturn(request);

        ServiceRequest result = serviceRequestService.assignTechnician(10L, 1L);

        assertEquals(ServiceRequestStatus.ASSIGNED, result.getStatus());
        assertEquals(technician, result.getAssignedTechnician());
    }

    @Test
    void startWork_onOpenRequest_isRejected() {
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> serviceRequestService.startWork(10L));

        assertEquals("Cannot change service request 10 from OPEN to IN_PROGRESS", ex.getMessage());
        verify(equipmentService, never()).updateStatus(any(), any());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void startWork_onAssignedRequest_putsEquipmentUnderMaintenance() {
        request.setStatus(ServiceRequestStatus.ASSIGNED);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        serviceRequestService.startWork(10L);

        assertEquals(ServiceRequestStatus.IN_PROGRESS, request.getStatus());
        verify(equipmentService).updateStatus(equipment, EquipmentStatus.UNDER_MAINTENANCE);
    }

    @Test
    void completeRequest_withNoOtherActiveRepairs_returnsEquipmentToOperational() {
        request.setStatus(ServiceRequestStatus.IN_PROGRESS);
        equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(requestRepository.existsByEquipmentIdAndStatusInAndIdNot(eq(1L), anyList(), eq(10L)))
                .thenReturn(false);
        when(requestRepository.save(request)).thenReturn(request);

        serviceRequestService.completeRequest(10L, new CompleteServiceRequest("Replaced thermostat"));

        assertEquals(ServiceRequestStatus.COMPLETED, request.getStatus());
        assertEquals("Replaced thermostat", request.getResolutionNotes());
        assertNotNull(request.getCompletedAt());
        verify(equipmentService).updateStatus(equipment, EquipmentStatus.OPERATIONAL);
    }

    @Test
    void completeRequest_whileAnotherRepairIsActive_keepsEquipmentUnderMaintenance() {
        request.setStatus(ServiceRequestStatus.IN_PROGRESS);
        equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(requestRepository.existsByEquipmentIdAndStatusInAndIdNot(eq(1L), anyList(), eq(10L)))
                .thenReturn(true);
        when(requestRepository.save(request)).thenReturn(request);

        serviceRequestService.completeRequest(10L, new CompleteServiceRequest("Replaced thermostat"));

        assertEquals(ServiceRequestStatus.COMPLETED, request.getStatus());
        verify(equipmentService, never()).updateStatus(any(), any());
    }
}