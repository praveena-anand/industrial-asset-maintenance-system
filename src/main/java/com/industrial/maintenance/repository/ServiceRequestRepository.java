package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.ServiceRequest;
import com.industrial.maintenance.entity.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByStatus(ServiceRequestStatus status);

    List<ServiceRequest> findByEquipmentId(Long equipmentId);

    List<ServiceRequest> findByAssignedTechnicianId(Long technicianId);

    boolean existsByEquipmentIdAndStatusInAndIdNot(Long equipmentId,
                                                   List<ServiceRequestStatus> statuses,
                                                   Long id);
}