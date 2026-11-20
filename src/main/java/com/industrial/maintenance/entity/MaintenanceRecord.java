package com.industrial.maintenance.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Employee technician;

    @ManyToOne
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;          // set if this fixed a breakdown

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private MaintenanceSchedule maintenanceSchedule; // set if this was planned maintenance

    @Column(nullable = false, length = 2000)
    private String workDescription;

    @Column(nullable = false)
    private LocalDate performedDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal laborCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;                   // labour + all parts

    @OneToMany(mappedBy = "maintenanceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceRecordPart> parts = new ArrayList<>();

    public void addPart(MaintenanceRecordPart part) {
        part.setMaintenanceRecord(this);
        parts.add(part);
    }

    public Long getId() { return id; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public Employee getTechnician() { return technician; }
    public void setTechnician(Employee technician) { this.technician = technician; }

    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public void setServiceRequest(ServiceRequest serviceRequest) { this.serviceRequest = serviceRequest; }

    public MaintenanceSchedule getMaintenanceSchedule() { return maintenanceSchedule; }
    public void setMaintenanceSchedule(MaintenanceSchedule maintenanceSchedule) { this.maintenanceSchedule = maintenanceSchedule; }

    public String getWorkDescription() { return workDescription; }
    public void setWorkDescription(String workDescription) { this.workDescription = workDescription; }

    public LocalDate getPerformedDate() { return performedDate; }
    public void setPerformedDate(LocalDate performedDate) { this.performedDate = performedDate; }

    public BigDecimal getLaborCost() { return laborCost; }
    public void setLaborCost(BigDecimal laborCost) { this.laborCost = laborCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public List<MaintenanceRecordPart> getParts() { return parts; }
}