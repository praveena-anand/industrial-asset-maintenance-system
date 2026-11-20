package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByEquipmentCode(String equipmentCode);

    boolean existsBySerialNumber(String serialNumber);
}