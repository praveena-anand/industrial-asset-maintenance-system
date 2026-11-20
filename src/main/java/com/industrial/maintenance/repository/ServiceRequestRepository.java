package com.industrial.maintenance.repository;

import com.industrial.maintenance.dto.EquipmentBreakdownCount;
import com.industrial.maintenance.dto.TechnicianWorkload;
import com.industrial.maintenance.entity.ServiceRequest;
import com.industrial.maintenance.entity.ServiceRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByStatus(ServiceRequestStatus status);

    List<ServiceRequest> findByEquipmentId(Long equipmentId);

    List<ServiceRequest> findByAssignedTechnicianId(Long technicianId);

    boolean existsByEquipmentIdAndStatusInAndIdNot(Long equipmentId,
                                                   List<ServiceRequestStatus> statuses,
                                                   Long id);

    long countByStatusIn(List<ServiceRequestStatus> statuses);

    @Query("""
            SELECT new com.industrial.maintenance.dto.EquipmentBreakdownCount(
                       e.equipmentCode, e.name, COUNT(sr))
            FROM ServiceRequest sr JOIN sr.equipment e
            GROUP BY e.id, e.equipmentCode, e.name
            ORDER BY COUNT(sr) DESC
            """)
    List<EquipmentBreakdownCount> findMostFrequentlyFailingEquipment(Pageable pageable);

    @Query("""
            SELECT new com.industrial.maintenance.dto.TechnicianWorkload(
                       t.fullName, COUNT(sr))
            FROM ServiceRequest sr JOIN sr.assignedTechnician t
            WHERE sr.status IN :statuses
            GROUP BY t.id, t.fullName
            ORDER BY COUNT(sr) DESC
            """)
    List<TechnicianWorkload> findTechnicianWorkload(@Param("statuses") List<ServiceRequestStatus> statuses);
}