package com.industrial.maintenance.entity;

public enum AlertStatus {
    OPEN,            // just raised, nobody has looked at it yet
    ACKNOWLEDGED,    // someone has seen it and is dealing with it
    RESOLVED         // the problem is fixed
}