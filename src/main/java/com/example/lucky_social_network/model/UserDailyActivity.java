package com.example.lucky_social_network.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Duration;
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


    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private int loginCount;

    private LocalDateTime lastLoginTime;

    @Column(name = "first_activity")
    private LocalDateTime firstActivity;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    private int actionsCount;  // Количество действий пользователя

    private long timeSpentSeconds;  // Время, проведенное в системе

    // Конструктор для создания новой активности
    public UserDailyActivity(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        this.loginCount = 0;
        this.actionsCount = 0;
        this.timeSpentSeconds = 0;
        this.firstActivity = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }

    public void incrementLoginCount() {
        // Увеличиваем счетчик входов
        this.loginCount++;

        // Обновляем время последнего входа
        LocalDateTime now = LocalDateTime.now();
        this.lastLoginTime = now;

        // Если это первая активность за день
        if (this.firstActivity == null) {
            this.firstActivity = now;
        }

        // Обновляем время последней активности
        this.lastActivity = now;

        // Пересчитываем общее время активности
        if (this.firstActivity != null && this.lastActivity != null) {
            this.timeSpentSeconds = Duration.between(this.firstActivity, this.lastActivity).getSeconds();
        }

        // Также считаем это за действие
        this.actionsCount++;
    }

    public void incrementActionsCount() {
        this.actionsCount++;
        LocalDateTime now = LocalDateTime.now();

        if (this.firstActivity == null) {
            this.firstActivity = now;
        }

        this.lastActivity = now;

        // Обновляем общее время
        if (this.firstActivity != null) {
            this.timeSpentSeconds = Duration.between(this.firstActivity, this.lastActivity).getSeconds();
        }
    }

    public void updateTimeSpent(long seconds) {
        LocalDateTime now = LocalDateTime.now();

        if (this.firstActivity == null) {
            this.firstActivity = now;
        }

        this.lastActivity = now;
        this.timeSpentSeconds = seconds;

        // Обновляем общее время на основе первой и последней активности
        if (this.firstActivity != null && this.lastActivity != null) {
            long totalSeconds = Duration.between(this.firstActivity, this.lastActivity).getSeconds();
            if (totalSeconds > this.timeSpentSeconds) {
                this.timeSpentSeconds = totalSeconds;
            }
        }
    }
}

