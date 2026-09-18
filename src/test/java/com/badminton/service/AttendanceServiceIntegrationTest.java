package com.badminton.service;

import com.badminton.TestPrincipalFactory;
import com.badminton.entity.Attendance;
import com.badminton.entity.AttendanceStatus;
import com.badminton.entity.Event;
import com.badminton.entity.Member;
import com.badminton.entity.Role;
import com.badminton.exception.EventNotFoundException;
import com.badminton.repository.AttendanceRepository;
import com.badminton.repository.EventRepository;
import com.badminton.repository.MemberRepository;
import com.badminton.security.MemberPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AttendanceServiceIntegrationTest {

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EventRepository eventRepository;

    private Member memberA;
    private Member memberB;
    private Event event;

    @BeforeEach
    void setUp() {
        memberA = saveMember("UA", "会員A", Role.USER);
        memberB = saveMember("UB", "会員B", Role.USER);
        event = saveEvent();
    }

    @Test
    void registerAttendingCreatesAttendance() {
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.ATTENDING, principal(memberA));

        Attendance attendance = attendanceRepository.findByEventIdAndMemberId(event.getId(), memberA.getId()).orElseThrow();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ATTENDING);
    }

    @Test
    void registerAbsentCreatesAttendance() {
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.ABSENT, principal(memberA));

        assertThat(attendanceRepository.findByEventIdAndMemberId(event.getId(), memberA.getId())).isPresent();
        assertThat(attendanceRepository.findByEventIdAndMemberId(event.getId(), memberA.getId()).orElseThrow().getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    void registerUndecidedCreatesAttendance() {
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.UNDECIDED, principal(memberA));

        assertThat(attendanceRepository.findByEventIdAndMemberId(event.getId(), memberA.getId()).orElseThrow().getStatus()).isEqualTo(AttendanceStatus.UNDECIDED);
    }

    @Test
    void updatingAttendanceKeepsSingleRecord() {
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.ATTENDING, principal(memberA));
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.ABSENT, principal(memberA));

        assertThat(attendanceRepository.findAll()).hasSize(1);
        assertThat(attendanceRepository.findByEventIdAndMemberId(event.getId(), memberA.getId()).orElseThrow().getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    void otherMemberCannotOverwriteExistingAttendanceBecauseOwnPrincipalIsUsed() {
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.ATTENDING, principal(memberA));
        attendanceService.updateMyAttendance(event.getId(), AttendanceStatus.ABSENT, principal(memberB));

        assertThat(attendanceRepository.findAll()).hasSize(2);
        assertThat(attendanceRepository.findByEventIdAndMemberId(event.getId(), memberA.getId()).orElseThrow().getStatus()).isEqualTo(AttendanceStatus.ATTENDING);
        assertThat(attendanceRepository.findByEventIdAndMemberId(event.getId(), memberB.getId()).orElseThrow().getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    void nonexistentEventThrowsException() {
        assertThatThrownBy(() -> attendanceService.updateMyAttendance(99999L, AttendanceStatus.ATTENDING, principal(memberA)))
                .isInstanceOf(EventNotFoundException.class);
    }

    private MemberPrincipal principal(Member member) {
        return TestPrincipalFactory.principal(member.getId(), member.getLineUserId(), member.getDisplayName(), member.getRole());
    }

    private Member saveMember(String lineUserId, String displayName, Role role) {
        Member member = new Member();
        member.setLineUserId(lineUserId);
        member.setDisplayName(displayName);
        member.setRole(role);
        member.setEnabled(true);
        return memberRepository.save(member);
    }

    private Event saveEvent() {
        Event event = new Event();
        event.setEventDate(LocalDate.now().plusDays(7));
        event.setStartTime(LocalTime.of(18, 0));
        event.setEndTime(LocalTime.of(21, 0));
        event.setPlace("市民体育館");
        event.setCapacity(20);
        event.setDescription("基礎練習");
        return eventRepository.save(event);
    }
}
