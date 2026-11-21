package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.MaintenanceScheduleRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.MaintenanceScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceScheduleService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduleService.class);

    private final MaintenanceScheduleRepository scheduleRepository;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;
    private final SensorReadingService sensorReadingService;

    public MaintenanceScheduleService(MaintenanceScheduleRepository scheduleRepository,
                                      EquipmentService equipmentService,
                                      EmployeeService employeeService,
                                      SensorReadingService sensorReadingService) {
        this.scheduleRepository = scheduleRepository;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
        this.sensorReadingService = sensorReadingService;
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

        if (request.frequencyHours() != null) {
            Double currentHours = sensorReadingService.getCurrentOperatingHours(equipment.getId());
            double startingHours = currentHours != null ? currentHours : 0.0;
            schedule.setFrequencyHours(request.frequencyHours());
            schedule.setNextDueHours(startingHours + request.frequencyHours());
        }

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

    /** Active schedules that are overdue by date OR by operating hours. */
    public List<MaintenanceSchedule> getOverdueSchedules() {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findByActiveTrue().stream()
                .filter(schedule -> schedule.isOverdue(today, getCurrentHours(schedule)))
                .toList();
    }

    public long countAllOverdue() {
        return getOverdueSchedules().size();
    }

    public long countOverdueForEquipment(Long equipmentId) {
        LocalDate today = LocalDate.now();
        Double currentHours = sensorReadingService.getCurrentOperatingHours(equipmentId);
        return scheduleRepository.findByEquipmentIdAndActiveTrue(equipmentId).stream()
                .filter(schedule -> schedule.isOverdue(today, currentHours))
                .count();
    }

    /** The current operating hours of the schedule's machine, or null if it has never reported. */
    public Double getCurrentHours(MaintenanceSchedule schedule) {
        return sensorReadingService.getCurrentOperatingHours(schedule.getEquipment().getId());
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

        if (schedule.getFrequencyHours() != null) {
            Double currentHours = sensorReadingService.getCurrentOperatingHours(schedule.getEquipment().getId());
            if (currentHours != null) {
                schedule.setLastCompletedHours(currentHours);
                schedule.setNextDueHours(currentHours + schedule.getFrequencyHours());
            } else {
                log.warn("Schedule {} completed but equipment {} has no sensor readings; hour-based due point unchanged",
                        id, schedule.getEquipment().getEquipmentCode());
            }
        }

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