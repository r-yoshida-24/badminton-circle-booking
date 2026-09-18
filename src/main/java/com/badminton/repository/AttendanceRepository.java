package com.badminton.repository;

import com.badminton.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEventIdAndMemberId(Long eventId, Long memberId);

    List<Attendance> findByEventIdIn(Collection<Long> eventIds);

    List<Attendance> findByEventIdOrderByMemberDisplayNameAsc(Long eventId);
}
