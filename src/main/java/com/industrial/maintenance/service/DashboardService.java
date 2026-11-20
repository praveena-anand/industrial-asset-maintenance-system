package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.DashboardSummary;
import com.industrial.maintenance.entity.EquipmentStatus;
import com.industrial.maintenance.entity.ServiceRequestStatus;
import com.industrial.maintenance.repository.EquipmentRepository;
import com.industrial.maintenance.repository.MaintenanceRecordRepository;
import com.industrial.maintenance.repository.MaintenanceScheduleRepository;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private static final List<ServiceRequestStatus> UNRESOLVED_STATUSES = List.of(
            ServiceRequestStatus.OPEN,
            ServiceRequestStatus.ASSIGNED,
            ServiceRequestStatus.IN_PROGRESS,
            ServiceRequestStatus.ON_HOLD);

    private static final List<ServiceRequestStatus> TECHNICIAN_ACTIVE_STATUSES = List.of(
            ServiceRequestStatus.ASSIGNED,
            ServiceRequestStatus.IN_PROGRESS,
            ServiceRequestStatus.ON_HOLD);

    private final EquipmentRepository equipmentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final MaintenanceScheduleRepository scheduleRepository;
    private final MaintenanceRecordRepository recordRepository;

    public DashboardService(EquipmentRepository equipmentRepository,
                            ServiceRequestRepository serviceRequestRepository,
                            MaintenanceScheduleRepository scheduleRepository,
                            MaintenanceRecordRepository recordRepository) {
        this.equipmentRepository = equipmentRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.scheduleRepository = scheduleRepository;
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        BigDecimal totalSpend = recordRepository.sumAllTotalCost();

        return new DashboardSummary(
                equipmentRepository.count(),
                equipmentRepository.countByStatus(EquipmentStatus.OPERATIONAL),
                equipmentRepository.countByStatus(EquipmentStatus.UNDER_MAINTENANCE),
                serviceRequestRepository.countByStatusIn(UNRESOLVED_STATUSES),
                scheduleRepository.countByActiveTrueAndNextDueDateBefore(LocalDate.now()),
                totalSpend != null ? totalSpend : BigDecimal.ZERO,
                serviceRequestRepository.findMostFrequentlyFailingEquipment(PageRequest.of(0, 5)),
                serviceRequestRepository.findTechnicianWorkload(TECHNICIAN_ACTIVE_STATUSES)
        );
    }
}