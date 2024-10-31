package com.example.lucky_social_network.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_daily_activity")
public class UserDailyActivity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private int loginCount;

    private LocalDateTime lastLoginTime;

    private int actionsCount;  // Количество действий пользователя

    private long timeSpentSeconds;  // Время, проведенное в системе

    // Конструктор для создания новой активности
    public UserDailyActivity(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        this.loginCount = 0;
        this.actionsCount = 0;
        this.timeSpentSeconds = 0;
    }

    // Методы для обновления статистики
    public void incrementLoginCount() {
        this.loginCount++;
        this.lastLoginTime = LocalDateTime.now();
    }

    public void incrementActionsCount() {
        this.actionsCount++;
    }

    public void updateTimeSpent(long seconds) {
        this.timeSpentSeconds += seconds;
    }
}

