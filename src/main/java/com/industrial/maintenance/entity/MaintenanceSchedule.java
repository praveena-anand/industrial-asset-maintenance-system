package com.industrial.maintenance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_schedules")
public class MaintenanceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private String taskName;            // e.g. Engine inspection

    private String description;

    @Column(nullable = false)
    private Integer frequencyDays;      // e.g. 180 = every six months

    @Column(nullable = false)
    private LocalDate nextDueDate;

    private LocalDate lastCompletedDate;

    private Double frequencyHours;        // optional: e.g. 500.0 = every 500 operating hours

    private Double nextDueHours;          // the operating-hours reading at which it is next due

    private Double lastCompletedHours;    // operating hours when it was last completed

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Employee assignedTechnician;    // optional

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getFrequencyDays() { return frequencyDays; }
    public void setFrequencyDays(Integer frequencyDays) { this.frequencyDays = frequencyDays; }

    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }

    public LocalDate getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(LocalDate lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }

    public Double getFrequencyHours() { return frequencyHours; }
    public void setFrequencyHours(Double frequencyHours) { this.frequencyHours = frequencyHours; }

    public Double getNextDueHours() { return nextDueHours; }
    public void setNextDueHours(Double nextDueHours) { this.nextDueHours = nextDueHours; }

    public Double getLastCompletedHours() { return lastCompletedHours; }
    public void setLastCompletedHours(Double lastCompletedHours) { this.lastCompletedHours = lastCompletedHours; }

    public Employee getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(Employee assignedTechnician) { this.assignedTechnician = assignedTechnician; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isDueByDate(LocalDate today) {
        return active && nextDueDate != null && nextDueDate.isBefore(today);
    }

    public boolean isDueByHours(Double currentHours) {
        return active && nextDueHours != null && currentHours != null && currentHours >= nextDueHours;
    }

    public boolean isOverdue(LocalDate today, Double currentHours) {
        return isDueByDate(today) || isDueByHours(currentHours);
    }
}