package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.EquipmentRequest;
import com.industrial.maintenance.entity.Equipment;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.entity.Factory;
import com.industrial.maintenance.exception.DuplicateEquipmentException;
import com.industrial.maintenance.exception.EquipmentNotFoundException;
import com.industrial.maintenance.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private FactoryService factoryService;

    @InjectMocks
    private EquipmentService equipmentService;

    private EquipmentRequest validRequest() {
        return new EquipmentRequest("EQ2001", "Wheel Loader", "950GC",
                "SER2001", 1L, LocalDate.of(2026, 1, 1), null);
    }

    @Test
    void registerEquipment_withValidData_savesItAsOperational() {
        // Arrange: set up the fake repository's answers
        Factory factory = new Factory();
        factory.setId(1L);
        when(equipmentRepository.existsByEquipmentCode("EQ2001")).thenReturn(false);
        when(equipmentRepository.existsBySerialNumber("SER2001")).thenReturn(false);
        when(factoryService.getFactoryById(1L)).thenReturn(factory);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(call -> call.getArgument(0));

        // Act: call the method being tested
        Equipment result = equipmentService.registerEquipment(validRequest());

        // Assert: check the outcome
        assertEquals("EQ2001", result.getEquipmentCode());
        assertEquals(EquipmentStatus.OPERATIONAL, result.getStatus());
        assertEquals(factory, result.getFactory());
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void registerEquipment_withDuplicateCode_isRejected() {
        when(equipmentRepository.existsByEquipmentCode("EQ2001")).thenReturn(true);

        DuplicateEquipmentException ex = assertThrows(DuplicateEquipmentException.class,
                () -> equipmentService.registerEquipment(validRequest()));

        assertEquals("Equipment code EQ2001 already exists", ex.getMessage());
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void registerEquipment_withDuplicateSerialNumber_isRejected() {
        when(equipmentRepository.existsByEquipmentCode("EQ2001")).thenReturn(false);
        when(equipmentRepository.existsBySerialNumber("SER2001")).thenReturn(true);

        assertThrows(DuplicateEquipmentException.class,
                () -> equipmentService.registerEquipment(validRequest()));

        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void getEquipmentById_whenItExists_returnsIt() {
        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setEquipmentCode("EQ2001");
        when(equipmentRepository.findById(5L)).thenReturn(Optional.of(equipment));

        Equipment result = equipmentService.getEquipmentById(5L);

        assertEquals("EQ2001", result.getEquipmentCode());
    }

    @Test
    void getEquipmentById_whenMissing_throwsNotFound() {
        when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

        EquipmentNotFoundException ex = assertThrows(EquipmentNotFoundException.class,
                () -> equipmentService.getEquipmentById(99L));

        assertEquals("Equipment with ID 99 not found", ex.getMessage());
    }

    @Test
    void retireEquipment_setsStatusToRetired() {
        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setStatus(EquipmentStatus.OPERATIONAL);
        when(equipmentRepository.findById(5L)).thenReturn(Optional.of(equipment));

        equipmentService.retireEquipment(5L);

        assertEquals(EquipmentStatus.RETIRED, equipment.getStatus());
        verify(equipmentRepository).save(equipment);
    }
}