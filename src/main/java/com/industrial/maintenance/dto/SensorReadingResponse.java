package com.industrial.maintenance.dto;

import com.industrial.maintenance.entity.SensorReading;

import java.time.LocalDateTime;

public record SensorReadingResponse(
        Long id,
        Long equipmentId,
        String equipmentCode,
        LocalDateTime recordedAt,
        Double temperatureCelsius,
        Double vibrationMmPerSec,
        Double oilPressureBar,
        Double operatingHours,
        Boolean anomaly,
        Double anomalyScore,
        String anomalyModelVersion
) {

    public static SensorReadingResponse from(SensorReading reading) {
        return new SensorReadingResponse(
                reading.getId(),
                reading.getEquipment().getId(),
                reading.getEquipment().getEquipmentCode(),
                reading.getRecordedAt(),
                reading.getTemperatureCelsius(),
                reading.getVibrationMmPerSec(),
                reading.getOilPressureBar(),
                reading.getOperatingHours(),
                reading.getAnomaly(),
                reading.getAnomalyScore(),
                reading.getAnomalyModelVersion()
        );
    }
}