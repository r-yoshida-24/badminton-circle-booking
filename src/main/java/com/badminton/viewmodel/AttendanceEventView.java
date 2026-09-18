package com.badminton.viewmodel;

import com.badminton.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceEventView(
        Long eventId,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        String place,
        Integer capacity,
        String description,
        AttendanceStatus myStatus,
        AttendanceCountView counts
) {
}
