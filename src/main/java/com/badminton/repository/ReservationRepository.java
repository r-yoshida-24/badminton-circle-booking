package com.badminton.repository;

import com.badminton.entity.Reservation;
import com.badminton.entity.Gym;
import com.badminton.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGym(Gym gym);
    List<Reservation> findByUser(User user);
    List<Reservation> findByGymAndStatus(Gym gym, String status);
    List<Reservation> findByUserAndStatus(User user, String status);
    Optional<Reservation> findByGymAndUserAndStatus(Gym gym, User user, String status);
}
