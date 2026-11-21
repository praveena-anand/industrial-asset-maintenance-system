package com.industrial.maintenance.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceScheduleTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 21);

    private MaintenanceSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new MaintenanceSchedule();
        schedule.setActive(true);
        schedule.setNextDueDate(TODAY.plusDays(30));   // not due by date
        schedule.setNextDueHours(500.0);
    }

    @Test
    void notOverdue_beforeDateAndBeforeHours() {
        assertFalse(schedule.isOverdue(TODAY, 450.0));
    }

    @Test
    void overdue_whenHoursReached_evenIfDateNotPassed() {
        assertTrue(schedule.isDueByHours(500.0));
        assertTrue(schedule.isOverdue(TODAY, 520.0));
        assertFalse(schedule.isDueByDate(TODAY));
    }

    @Test
    void overdue_whenDatePassed_evenWithoutReadings() {
        schedule.setNextDueDate(TODAY.minusDays(1));
        assertTrue(schedule.isOverdue(TODAY, null));
    }

    @Test
    void scheduleWithoutHourInterval_isNeverDueByHours() {
        schedule.setNextDueHours(null);
        assertFalse(schedule.isDueByHours(99999.0));
    }

    @Test
    void inactiveSchedule_isNeverOverdue() {
        schedule.setActive(false);
        schedule.setNextDueDate(TODAY.minusDays(10));
        assertFalse(schedule.isOverdue(TODAY, 99999.0));
    }
}