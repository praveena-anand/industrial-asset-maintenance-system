package com.industrial.maintenance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "maintenance_record_parts")
public class MaintenanceRecordPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "maintenance_record_id", nullable = false)
    private MaintenanceRecord maintenanceRecord;

    @ManyToOne
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;     // price at the time of the repair

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }

    public MaintenanceRecord getMaintenanceRecord() { return maintenanceRecord; }
    public void setMaintenanceRecord(MaintenanceRecord maintenanceRecord) { this.maintenanceRecord = maintenanceRecord; }

    public SparePart getSparePart() { return sparePart; }
    public void setSparePart(SparePart sparePart) { this.sparePart = sparePart; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}