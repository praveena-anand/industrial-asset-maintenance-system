package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findByEquipmentIdOrderByPerformedDateDesc(Long equipmentId);

    long countByEquipmentId(Long equipmentId);

    @Query("SELECT SUM(r.totalCost) FROM MaintenanceRecord r WHERE r.equipment.id = :equipmentId")
    BigDecimal sumTotalCostByEquipmentId(@Param("equipmentId") Long equipmentId);

    @Query("SELECT SUM(r.totalCost) FROM MaintenanceRecord r")
    BigDecimal sumAllTotalCost();
}