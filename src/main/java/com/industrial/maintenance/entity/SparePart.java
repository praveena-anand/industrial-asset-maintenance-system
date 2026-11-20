package com.industrial.maintenance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "spare_parts")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Part number is required")
    @Column(nullable = false, unique = true)
    private String partNumber;      // e.g. PRT-THERM-01

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;            // e.g. Engine thermostat

    @NotNull(message = "Unit price is required")
    @PositiveOrZero(message = "Unit price cannot be negative")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;   // e.g. 1500.00

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}