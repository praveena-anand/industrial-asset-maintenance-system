package com.industrial.maintenance.entity;

public enum ServiceRequestStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    CANCELLED;

    public boolean canMoveTo(ServiceRequestStatus next) {
        return switch (this) {
            case OPEN        -> next == ASSIGNED || next == CANCELLED;
            case ASSIGNED    -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == ON_HOLD || next == COMPLETED;
            case ON_HOLD     -> next == IN_PROGRESS || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}