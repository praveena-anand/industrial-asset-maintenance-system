package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    Optional<SensorReading> findFirstByEquipmentIdOrderByRecordedAtDesc(Long equipmentId);

    List<SensorReading> findTop20ByEquipmentIdOrderByRecordedAtDesc(Long equipmentId);

    List<SensorReading> findByEquipmentIdAndRecordedAtAfterOrderByRecordedAtAsc(Long equipmentId,
                                                                                LocalDateTime since);
}