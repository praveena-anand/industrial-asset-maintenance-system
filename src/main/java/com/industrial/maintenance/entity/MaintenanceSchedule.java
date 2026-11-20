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

    public Employee getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(Employee assignedTechnician) { this.assignedTechnician = assignedTechnician; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}