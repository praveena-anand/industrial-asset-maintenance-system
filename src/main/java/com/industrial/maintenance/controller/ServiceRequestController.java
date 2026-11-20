package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.CompleteServiceRequest;
import com.industrial.maintenance.dto.ServiceRequestRequest;
import com.industrial.maintenance.entity.ServiceRequest;
import com.industrial.maintenance.entity.ServiceRequestStatus;
import com.industrial.maintenance.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Service Requests", description = "Report breakdowns and manage the repair workflow")
@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService requestService;

    public ServiceRequestController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRequest create(@Valid @RequestBody ServiceRequestRequest request) {
        return requestService.createRequest(request);
    }

    @GetMapping
    public List<ServiceRequest> getAll(@RequestParam(required = false) ServiceRequestStatus status,
                                       @RequestParam(required = false) Long equipmentId,
                                       @RequestParam(required = false) Long technicianId) {
        return requestService.getRequests(status, equipmentId, technicianId);
    }

    @GetMapping("/{id}")
    public ServiceRequest getById(@PathVariable Long id) {
        return requestService.getRequestById(id);
    }

    @PatchMapping("/{id}/assign")
    public ServiceRequest assign(@PathVariable Long id, @RequestParam Long technicianId) {
        return requestService.assignTechnician(id, technicianId);
    }

    @PostMapping("/{id}/start")
    public ServiceRequest start(@PathVariable Long id) {
        return requestService.startWork(id);
    }

    @PostMapping("/{id}/hold")
    public ServiceRequest hold(@PathVariable Long id) {
        return requestService.putOnHold(id);
    }

    @PostMapping("/{id}/resume")
    public ServiceRequest resume(@PathVariable Long id) {
        return requestService.resumeWork(id);
    }

    @PostMapping("/{id}/complete")
    public ServiceRequest complete(@PathVariable Long id,
                                   @Valid @RequestBody CompleteServiceRequest completion) {
        return requestService.completeRequest(id, completion);
    }

    @PostMapping("/{id}/cancel")
    public ServiceRequest cancel(@PathVariable Long id) {
        return requestService.cancelRequest(id);
    }
}