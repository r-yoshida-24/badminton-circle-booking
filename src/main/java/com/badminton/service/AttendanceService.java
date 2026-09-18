package com.badminton.service;

import com.badminton.entity.Attendance;
import com.badminton.entity.AttendanceStatus;
import com.badminton.entity.Event;
import com.badminton.entity.Member;
import com.badminton.exception.AttendanceException;
import com.badminton.repository.AttendanceRepository;
import com.badminton.security.MemberPrincipal;
import com.badminton.viewmodel.AttendanceCountView;
import com.badminton.viewmodel.AttendanceEventView;
import com.badminton.viewmodel.EventParticipantView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventService eventService;
    private final MemberService memberService;

    public AttendanceService(AttendanceRepository attendanceRepository, EventService eventService, MemberService memberService) {
        this.attendanceRepository = attendanceRepository;
        this.eventService = eventService;
        this.memberService = memberService;
    }

    public List<AttendanceEventView> getUpcomingAttendanceViews(MemberPrincipal principal) {
        Member currentMember = memberService.getCurrentMember(principal);
        List<Event> events = eventService.findUpcomingEvents();
        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Attendance>> attendancesByEventId = attendanceRepository.findByEventIdIn(
                        events.stream().map(Event::getId).toList()
                ).stream()
                .collect(Collectors.groupingBy(attendance -> attendance.getEvent().getId()));

        return events.stream()
                .map(event -> toAttendanceEventView(event, currentMember, attendancesByEventId.getOrDefault(event.getId(), Collections.emptyList())))
                .toList();
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void updateMyAttendance(Long eventId, AttendanceStatus status, MemberPrincipal principal) {
        Event event = eventService.getEvent(eventId);
        Member member = memberService.getCurrentMember(principal);

        Attendance attendance = attendanceRepository.findByEventIdAndMemberId(eventId, member.getId())
                .orElseGet(() -> {
                    Attendance created = new Attendance();
                    created.setEvent(event);
                    created.setMember(member);
                    return created;
                });
        attendance.setStatus(status);

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException ex) {
            Attendance existing = attendanceRepository.findByEventIdAndMemberId(eventId, member.getId())
                    .orElseThrow(() -> new AttendanceException("出欠登録に失敗しました。", ex));
            existing.setStatus(status);
        }
    }

    public List<EventParticipantView> getEventParticipants(Long eventId) {
        eventService.getEvent(eventId);
        return attendanceRepository.findByEventIdOrderByMemberDisplayNameAsc(eventId).stream()
                .map(attendance -> new EventParticipantView(
                        attendance.getMember().getDisplayName(),
                        attendance.getMember().getLineUserId(),
                        attendance.getStatus()
                ))
                .toList();
    }

    private AttendanceEventView toAttendanceEventView(Event event, Member currentMember, List<Attendance> attendances) {
        AttendanceStatus myStatus = attendances.stream()
                .filter(attendance -> attendance.getMember().getId().equals(currentMember.getId()))
                .map(Attendance::getStatus)
                .findFirst()
                .orElse(AttendanceStatus.UNDECIDED);

        Map<AttendanceStatus, Long> counts = attendances.stream()
                .map(Attendance::getStatus)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new AttendanceEventView(
                event.getId(),
                event.getEventDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getPlace(),
                event.getCapacity(),
                event.getDescription(),
                myStatus,
                new AttendanceCountView(
                        counts.getOrDefault(AttendanceStatus.ATTENDING, 0L),
                        counts.getOrDefault(AttendanceStatus.ABSENT, 0L),
                        counts.getOrDefault(AttendanceStatus.UNDECIDED, 0L)
                )
        );
    }
}
