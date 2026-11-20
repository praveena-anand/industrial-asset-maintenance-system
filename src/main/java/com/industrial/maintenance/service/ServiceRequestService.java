package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.CompleteServiceRequest;
import com.industrial.maintenance.dto.ServiceRequestRequest;
import com.industrial.maintenance.entity.*;
import com.industrial.maintenance.exception.BusinessRuleException;
import com.industrial.maintenance.exception.ResourceNotFoundException;
import com.industrial.maintenance.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceRequestService {

    private static final List<ServiceRequestStatus> ACTIVE_REPAIR_STATUSES =
            List.of(ServiceRequestStatus.IN_PROGRESS, ServiceRequestStatus.ON_HOLD);

    private final ServiceRequestRepository requestRepository;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 EquipmentService equipmentService,
                                 EmployeeService employeeService) {
        this.requestRepository = requestRepository;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
    }

    public ServiceRequest createRequest(ServiceRequestRequest request) {
        Equipment equipment = equipmentService.getEquipmentById(request.equipmentId());
        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new BusinessRuleException("Equipment " + equipment.getEquipmentCode()
                    + " is retired and cannot receive service requests");
        }

        Employee reporter = employeeService.getEmployeeById(request.reportedById());
        if (reporter.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessRuleException(reporter.getFullName() + " is not an active employee");
        }

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setEquipment(equipment);
        serviceRequest.setReportedBy(reporter);
        serviceRequest.setProblemDescription(request.problemDescription());
        serviceRequest.setPriority(request.priority());
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        return requestRepository.save(serviceRequest);
    }

    public List<ServiceRequest> getRequests(ServiceRequestStatus status,
                                            Long equipmentId,
                                            Long technicianId) {
        if (status != null) {
            return requestRepository.findByStatus(status);
        }
        if (equipmentId != null) {
            return requestRepository.findByEquipmentId(equipmentId);
        }
        if (technicianId != null) {
            return requestRepository.findByAssignedTechnicianId(technicianId);
        }
        return requestRepository.findAll();
    }

    public ServiceRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service request with ID " + id + " not found"));
    }

    public ServiceRequest assignTechnician(Long id, Long technicianId) {
        ServiceRequest serviceRequest = getRequestById(id);
        Employee technician = employeeService.getActiveTechnician(technicianId);

        // Reassigning an already-assigned request keeps the status; otherwise it must move to ASSIGNED
        if (serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
            changeStatus(serviceRequest, ServiceRequestStatus.ASSIGNED);
        }
        serviceRequest.setAssignedTechnician(technician);
        return requestRepository.save(serviceRequest);
    }

    @Transactional
    public ServiceRequest startWork(Long id) {
        ServiceRequest serviceRequest = getRequestById(id);
        changeStatus(serviceRequest, ServiceRequestStatus.IN_PROGRESS);
        equipmentService.updateStatus(serviceRequest.getEquipment(), EquipmentStatus.UNDER_MAINTENANCE);
        return requestRepository.save(serviceRequest);
    }

    public ServiceRequest putOnHold(Long id) {
        ServiceRequest serviceRequest = getRequestById(id);
        changeStatus(serviceRequest, ServiceRequestStatus.ON_HOLD);
        return requestRepository.save(serviceRequest);
    }

    public ServiceRequest resumeWork(Long id) {
        ServiceRequest serviceRequest = getRequestById(id);
        changeStatus(serviceRequest, ServiceRequestStatus.IN_PROGRESS);
        return requestRepository.save(serviceRequest);
    }

    @Transactional
    public ServiceRequest completeRequest(Long id, CompleteServiceRequest completion) {
        ServiceRequest serviceRequest = getRequestById(id);
        changeStatus(serviceRequest, ServiceRequestStatus.COMPLETED);
        serviceRequest.setResolutionNotes(completion.resolutionNotes());
        serviceRequest.setCompletedAt(LocalDateTime.now());
        releaseEquipmentIfFree(serviceRequest);
        return requestRepository.save(serviceRequest);
    }

    @Transactional
    public ServiceRequest cancelRequest(Long id) {
        ServiceRequest serviceRequest = getRequestById(id);
        changeStatus(serviceRequest, ServiceRequestStatus.CANCELLED);
        releaseEquipmentIfFree(serviceRequest);
        return requestRepository.save(serviceRequest);
    }

    // Puts the machine back to OPERATIONAL only if no other repair on it is still underway
    private void releaseEquipmentIfFree(ServiceRequest serviceRequest) {
        Equipment equipment = serviceRequest.getEquipment();
        boolean otherRepairsActive = requestRepository.existsByEquipmentIdAndStatusInAndIdNot(
                equipment.getId(), ACTIVE_REPAIR_STATUSES, serviceRequest.getId());

        if (!otherRepairsActive && equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            equipmentService.updateStatus(equipment, EquipmentStatus.OPERATIONAL);
        }
    }

    private void changeStatus(ServiceRequest serviceRequest, ServiceRequestStatus next) {
        ServiceRequestStatus current = serviceRequest.getStatus();
        if (!current.canMoveTo(next)) {
            throw new BusinessRuleException("Cannot change service request " + serviceRequest.getId()
                    + " from " + current + " to " + next);
        }
        serviceRequest.setStatus(next);
    }
}