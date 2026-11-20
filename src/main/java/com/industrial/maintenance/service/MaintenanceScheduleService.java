package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.MaintenanceScheduleRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.MaintenanceScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceScheduleService {

    private final MaintenanceScheduleRepository scheduleRepository;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;

    public MaintenanceScheduleService(MaintenanceScheduleRepository scheduleRepository,
                                      EquipmentService equipmentService,
                                      EmployeeService employeeService) {
        this.scheduleRepository = scheduleRepository;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
    }

    public MaintenanceSchedule createSchedule(MaintenanceScheduleRequest request) {
        Equipment equipment = equipmentService.getEquipmentById(request.equipmentId());

        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new BusinessRuleException("Equipment " + equipment.getEquipmentCode()
                    + " is retired and cannot receive new maintenance tasks");
        }

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setEquipment(equipment);
        schedule.setTaskName(request.taskName());
        schedule.setDescription(request.description());
        schedule.setFrequencyDays(request.frequencyDays());
        schedule.setNextDueDate(request.nextDueDate());

        if (request.technicianId() != null) {
            schedule.setAssignedTechnician(employeeService.getActiveTechnician(request.technicianId()));
        }
        return scheduleRepository.save(schedule);
    }

    public List<MaintenanceSchedule> getSchedules(Long equipmentId) {
        if (equipmentId != null) {
            equipmentService.getEquipmentById(equipmentId);   // 404 if the machine doesn't exist
            return scheduleRepository.findByEquipmentId(equipmentId);
        }
        return scheduleRepository.findAll();
    }

    public MaintenanceSchedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance schedule with ID " + id + " not found"));
    }

    public List<MaintenanceSchedule> getOverdueSchedules() {
        return scheduleRepository.findByActiveTrueAndNextDueDateBefore(LocalDate.now());
    }

    public List<MaintenanceSchedule> getUpcomingSchedules(int days) {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findByActiveTrueAndNextDueDateBetween(today, today.plusDays(days));
    }

    public MaintenanceSchedule assignTechnician(Long id, Long technicianId) {
        MaintenanceSchedule schedule = getScheduleById(id);
        ensureActive(schedule);
        schedule.setAssignedTechnician(employeeService.getActiveTechnician(technicianId));
        return scheduleRepository.save(schedule);
    }

    public MaintenanceSchedule completeSchedule(Long id) {
        MaintenanceSchedule schedule = getScheduleById(id);
        ensureActive(schedule);

        LocalDate today = LocalDate.now();
        schedule.setLastCompletedDate(today);
        schedule.setNextDueDate(today.plusDays(schedule.getFrequencyDays()));

        return scheduleRepository.save(schedule);
    }

    public void deactivateSchedule(Long id) {
        MaintenanceSchedule schedule = getScheduleById(id);
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    private void ensureActive(MaintenanceSchedule schedule) {
        if (!schedule.isActive()) {
            throw new BusinessRuleException(
                    "Maintenance schedule " + schedule.getId() + " is inactive");
        }
    }
}