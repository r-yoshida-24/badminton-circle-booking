package com.badminton.repository;

import com.badminton.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAscStartTimeAsc(LocalDate eventDate);

    List<Event> findAllByOrderByEventDateAscStartTimeAsc();
}
