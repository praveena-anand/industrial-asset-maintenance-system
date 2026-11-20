package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.EquipmentCostSummary;
import com.industrial.maintenance.dto.MaintenanceRecordRequest;
import com.industrial.maintenance.dto.PartUsageRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.MaintenanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MaintenanceRecordService {

    private final MaintenanceRecordRepository recordRepository;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;
    private final ServiceRequestService serviceRequestService;
    private final MaintenanceScheduleService scheduleService;
    private final SparePartService sparePartService;

    public MaintenanceRecordService(MaintenanceRecordRepository recordRepository,
                                    EquipmentService equipmentService,
                                    EmployeeService employeeService,
                                    ServiceRequestService serviceRequestService,
                                    MaintenanceScheduleService scheduleService,
                                    SparePartService sparePartService) {
        this.recordRepository = recordRepository;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
        this.serviceRequestService = serviceRequestService;
        this.scheduleService = scheduleService;
        this.sparePartService = sparePartService;
    }

    @Transactional
    public MaintenanceRecord createRecord(MaintenanceRecordRequest request) {
        Equipment equipment = equipmentService.getEquipmentById(request.equipmentId());
        Employee technician = employeeService.getActiveTechnician(request.technicianId());

        MaintenanceRecord record = new MaintenanceRecord();
        record.setEquipment(equipment);
        record.setTechnician(technician);
        record.setWorkDescription(request.workDescription());
        record.setPerformedDate(request.performedDate());
        record.setLaborCost(request.laborCost());

        if (request.serviceRequestId() != null) {
            ServiceRequest serviceRequest = serviceRequestService.getRequestById(request.serviceRequestId());
            if (!serviceRequest.getEquipment().getId().equals(equipment.getId())) {
                throw new BusinessRuleException("Service request " + serviceRequest.getId()
                        + " belongs to a different machine");
            }
            if (serviceRequest.getStatus() != ServiceRequestStatus.COMPLETED) {
                throw new BusinessRuleException("Service request " + serviceRequest.getId()
                        + " must be COMPLETED before its work is recorded");
            }
            if (recordRepository.existsByServiceRequestId(serviceRequest.getId())) {
                throw new BusinessRuleException("Service request " + serviceRequest.getId()
                        + " already has a maintenance record");
            }
            record.setServiceRequest(serviceRequest);
        }

        if (request.scheduleId() != null) {
            MaintenanceSchedule schedule = scheduleService.getScheduleById(request.scheduleId());
            if (!schedule.getEquipment().getId().equals(equipment.getId())) {
                throw new BusinessRuleException("Maintenance schedule " + schedule.getId()
                        + " belongs to a different machine");
            }
            record.setMaintenanceSchedule(schedule);
        }

        BigDecimal partsCost = BigDecimal.ZERO;
        if (request.parts() != null) {
            for (PartUsageRequest usage : request.parts()) {
                SparePart sparePart = sparePartService.getPartById(usage.sparePartId());

                MaintenanceRecordPart line = new MaintenanceRecordPart();
                line.setSparePart(sparePart);
                line.setQuantity(usage.quantity());
                line.setUnitPrice(sparePart.getUnitPrice());   // snapshot of today's price
                record.addPart(line);

                partsCost = partsCost.add(line.getLineTotal());
            }
        }

        record.setTotalCost(request.laborCost().add(partsCost));
        return recordRepository.save(record);
    }

    public MaintenanceRecord getRecordById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance record with ID " + id + " not found"));
    }

    public List<MaintenanceRecord> getHistory(Long equipmentId) {
        equipmentService.getEquipmentById(equipmentId);   // 404 if the machine doesn't exist
        return recordRepository.findByEquipmentIdOrderByPerformedDateDesc(equipmentId);
    }

    public EquipmentCostSummary getCostSummary(Long equipmentId) {
        Equipment equipment = equipmentService.getEquipmentById(equipmentId);
        BigDecimal total = recordRepository.sumTotalCostByEquipmentId(equipmentId);

        return new EquipmentCostSummary(
                equipment.getId(),
                equipment.getEquipmentCode(),
                recordRepository.countByEquipmentId(equipmentId),
                total != null ? total : BigDecimal.ZERO
        );
    }
}