package com.badminton.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "gyms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column
    private String phoneNumber;
    @Column(nullable = false)
    private Integer courtCount;
    @Column(nullable = false)
    private Integer maxParticipants;
    @Column(nullable = false)
    private LocalDate bookingDate;
    @Column(nullable = false)
    private String timeSlot;
    @Column(nullable = false)
    private Integer currentParticipants = 0;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false)
    private boolean showParticipants = false;
    @Column(nullable = false)
    private Long createdBy;
    @Column
    private Long createdAt = System.currentTimeMillis();
}
