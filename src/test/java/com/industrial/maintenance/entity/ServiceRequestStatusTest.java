package com.industrial.maintenance.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.industrial.maintenance.entity.ServiceRequestStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestStatusTest {

    @Test
    void open_canBeAssignedOrCancelled() {
        assertTrue(OPEN.canMoveTo(ASSIGNED));
        assertTrue(OPEN.canMoveTo(CANCELLED));
    }

    @Test
    void open_cannotSkipStraightToWorkOrCompletion() {
        assertFalse(OPEN.canMoveTo(IN_PROGRESS));
        assertFalse(OPEN.canMoveTo(COMPLETED));
    }

    @Test
    void inProgress_canBePutOnHoldOrCompleted_butNotCancelled() {
        assertTrue(IN_PROGRESS.canMoveTo(ON_HOLD));
        assertTrue(IN_PROGRESS.canMoveTo(COMPLETED));
        assertFalse(IN_PROGRESS.canMoveTo(CANCELLED));
    }

    @ParameterizedTest
    @EnumSource(ServiceRequestStatus.class)
    void completed_isFinal(ServiceRequestStatus next) {
        assertFalse(COMPLETED.canMoveTo(next));
    }

    @ParameterizedTest
    @EnumSource(ServiceRequestStatus.class)
    void cancelled_isFinal(ServiceRequestStatus next) {
        assertFalse(CANCELLED.canMoveTo(next));
    }
}