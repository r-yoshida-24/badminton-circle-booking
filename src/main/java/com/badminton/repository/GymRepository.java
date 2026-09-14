package com.badminton.repository;

import com.badminton.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {
    List<Gym> findByActive(boolean active);
    List<Gym> findByBookingDate(LocalDate bookingDate);
    List<Gym> findByCreatedBy(Long createdBy);
    @Query("SELECT g FROM Gym g WHERE g.active = true AND g.currentParticipants < g.maxParticipants")
    List<Gym> findAvailableGyms();
}
