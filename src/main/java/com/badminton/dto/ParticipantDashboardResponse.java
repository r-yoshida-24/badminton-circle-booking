package com.badminton.dto;

import java.time.LocalDate;
import java.util.List;

public record ParticipantDashboardResponse(
        List<ParticipationItem> scheduledParticipations,
        List<ParticipationItem> waitingList,
        List<EventItem> events
) {
    public record ParticipationItem(
            Long reservationId,
            Long gymId,
            String gymName,
            String gymAddress,
            LocalDate bookingDate,
            String timeSlot
    ) {
    }

    public record EventItem(
            Long gymId,
            String gymName,
            String gymAddress,
            LocalDate bookingDate,
            String timeSlot,
            Integer participantCount,
            Integer capacity
    ) {
    }
}
