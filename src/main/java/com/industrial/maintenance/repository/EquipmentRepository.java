package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByEquipmentCode(String equipmentCode);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByEquipmentCodeAndIdNot(String equipmentCode, Long id);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    Optional<Equipment> findByEquipmentCode(String equipmentCode);

    Optional<Equipment> findBySerialNumber(String serialNumber);
}