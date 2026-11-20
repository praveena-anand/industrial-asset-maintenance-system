package com.industrial.maintenance.controller;

import com.industrial.maintenance.dto.CompleteServiceRequest;
import com.industrial.maintenance.dto.ServiceRequestRequest;
import com.industrial.maintenance.dto.ServiceRequestResponse;
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
    public ServiceRequestResponse create(@Valid @RequestBody ServiceRequestRequest request) {
        return ServiceRequestResponse.from(requestService.createRequest(request));
    }

    @GetMapping
    public List<ServiceRequestResponse> getAll(@RequestParam(required = false) ServiceRequestStatus status,
                                               @RequestParam(required = false) Long equipmentId,
                                               @RequestParam(required = false) Long technicianId) {
        return requestService.getRequests(status, equipmentId, technicianId).stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ServiceRequestResponse getById(@PathVariable Long id) {
        return ServiceRequestResponse.from(requestService.getRequestById(id));
    }

    @PatchMapping("/{id}/assign")
    public ServiceRequestResponse assign(@PathVariable Long id, @RequestParam Long technicianId) {
        return ServiceRequestResponse.from(requestService.assignTechnician(id, technicianId));
    }

    @PostMapping("/{id}/start")
    public ServiceRequestResponse start(@PathVariable Long id) {
        return ServiceRequestResponse.from(requestService.startWork(id));
    }

    @PostMapping("/{id}/hold")
    public ServiceRequestResponse hold(@PathVariable Long id) {
        return ServiceRequestResponse.from(requestService.putOnHold(id));
    }

    @PostMapping("/{id}/resume")
    public ServiceRequestResponse resume(@PathVariable Long id) {
        return ServiceRequestResponse.from(requestService.resumeWork(id));
    }

    @PostMapping("/{id}/complete")
    public ServiceRequestResponse complete(@PathVariable Long id,
                                           @Valid @RequestBody CompleteServiceRequest completion) {
        return ServiceRequestResponse.from(requestService.completeRequest(id, completion));
    }

    @PostMapping("/{id}/cancel")
    public ServiceRequestResponse cancel(@PathVariable Long id) {
        return ServiceRequestResponse.from(requestService.cancelRequest(id));
    }
}