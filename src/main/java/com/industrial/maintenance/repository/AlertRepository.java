package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.Alert;
import com.industrial.maintenance.entity.AlertStatus;
import com.industrial.maintenance.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findFirstByEquipmentIdAndTypeAndStatusIn(Long equipmentId,
                                                             AlertType type,
                                                             List<AlertStatus> statuses);

    List<Alert> findByStatusInOrderByCreatedAtDesc(List<AlertStatus> statuses);

    List<Alert> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);

    long countByEquipmentIdAndStatusIn(Long equipmentId, List<AlertStatus> statuses);

    long countByStatusIn(List<AlertStatus> statuses);
}