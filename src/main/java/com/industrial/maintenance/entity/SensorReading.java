package com.industrial.maintenance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings",
        indexes = @Index(name = "idx_readings_equipment_time", columnList = "equipment_id, recorded_at"))
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false)
    private Double temperatureCelsius;     // engine temperature, e.g. 85.5

    @Column(nullable = false)
    private Double vibrationMmPerSec;      // vibration velocity, e.g. 4.2

    @Column(nullable = false)
    private Double oilPressureBar;         // hydraulic oil pressure, e.g. 3.8

    @Column(nullable = false)
    private Double operatingHours;         // total running hours so far, e.g. 1240.5

    public Long getId() { return id; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public Double getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(Double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }

    public Double getVibrationMmPerSec() { return vibrationMmPerSec; }
    public void setVibrationMmPerSec(Double vibrationMmPerSec) { this.vibrationMmPerSec = vibrationMmPerSec; }

    public Double getOilPressureBar() { return oilPressureBar; }
    public void setOilPressureBar(Double oilPressureBar) { this.oilPressureBar = oilPressureBar; }

    public Double getOperatingHours() { return operatingHours; }
    public void setOperatingHours(Double operatingHours) { this.operatingHours = operatingHours; }
}