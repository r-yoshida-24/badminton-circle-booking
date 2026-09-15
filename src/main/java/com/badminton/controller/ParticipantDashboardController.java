package com.badminton.controller;

import com.badminton.dto.ParticipantDashboardResponse;
import com.badminton.entity.Gym;
import com.badminton.entity.Reservation;
import com.badminton.repository.GymRepository;
import com.badminton.repository.ReservationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
public class ParticipantDashboardController {
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_WAITING = "waiting";

    private final ReservationRepository reservationRepository;
    private final GymRepository gymRepository;

    public ParticipantDashboardController(
            ReservationRepository reservationRepository,
            GymRepository gymRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.gymRepository = gymRepository;
    }

    @GetMapping("/{userId}/dashboard")
    public ParticipantDashboardResponse getDashboard(@PathVariable Long userId) {
        List<ParticipantDashboardResponse.ParticipationItem> scheduledParticipations =
                reservationRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                        .stream()
                        .map(this::toParticipationItem)
                        .toList();

        List<ParticipantDashboardResponse.ParticipationItem> waitingList =
                reservationRepository.findByUserIdAndStatus(userId, STATUS_WAITING)
                        .stream()
                        .map(this::toParticipationItem)
                        .toList();

        List<ParticipantDashboardResponse.EventItem> events =
                gymRepository.findByActiveTrueOrderByBookingDateAscTimeSlotAsc()
                        .stream()
                        .map(this::toEventItem)
                        .toList();

        return new ParticipantDashboardResponse(scheduledParticipations, waitingList, events);
    }

    private ParticipantDashboardResponse.ParticipationItem toParticipationItem(Reservation reservation) {
        Gym gym = reservation.getGym();
        return new ParticipantDashboardResponse.ParticipationItem(
                reservation.getId(),
                gym.getId(),
                gym.getName(),
                gym.getAddress(),
                gym.getBookingDate(),
                gym.getTimeSlot()
        );
    }

    private ParticipantDashboardResponse.EventItem toEventItem(Gym gym) {
        return new ParticipantDashboardResponse.EventItem(
                gym.getId(),
                gym.getName(),
                gym.getAddress(),
                gym.getBookingDate(),
                gym.getTimeSlot(),
                gym.getCurrentParticipants(),
                gym.getMaxParticipants()
        );
    }
}
