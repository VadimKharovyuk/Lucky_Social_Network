package com.example.lucky_social_network.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_daily_activity")
@Data
public class UserDailyActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "activity_date")
    private LocalDate activityDate;

    @Column(name = "total_minutes")
    private Long totalMinutes = 0L;

    @Column(name = "session_count")
    private Integer sessionCount = 0;

    @Column(name = "first_activity")
    private LocalDateTime firstActivity;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;


}

