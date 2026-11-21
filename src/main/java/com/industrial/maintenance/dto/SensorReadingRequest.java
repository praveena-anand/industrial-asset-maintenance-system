package com.industrial.maintenance.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record SensorReadingRequest(

        @NotNull(message = "Temperature is required")
        @DecimalMin(value = "-50.0", message = "Temperature must be at least -50 °C")
        @DecimalMax(value = "200.0", message = "Temperature must be at most 200 °C")
        Double temperatureCelsius,

        @NotNull(message = "Vibration is required")
        @PositiveOrZero(message = "Vibration cannot be negative")
        @DecimalMax(value = "100.0", message = "Vibration must be at most 100 mm/s")
        Double vibrationMmPerSec,

        @NotNull(message = "Oil pressure is required")
        @PositiveOrZero(message = "Oil pressure cannot be negative")
        @DecimalMax(value = "50.0", message = "Oil pressure must be at most 50 bar")
        Double oilPressureBar,

        @NotNull(message = "Operating hours are required")
        @PositiveOrZero(message = "Operating hours cannot be negative")
        Double operatingHours,

        @PastOrPresent(message = "Reading time cannot be in the future")
        LocalDateTime recordedAt          // optional: defaults to "now" if not sent
) {}