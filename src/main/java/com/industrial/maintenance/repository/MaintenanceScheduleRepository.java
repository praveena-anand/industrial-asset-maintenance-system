package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    List<MaintenanceSchedule> findByEquipmentId(Long equipmentId);

    List<MaintenanceSchedule> findByActiveTrueAndNextDueDateBefore(LocalDate date);

    List<MaintenanceSchedule> findByActiveTrueAndNextDueDateBetween(LocalDate start, LocalDate end);
}