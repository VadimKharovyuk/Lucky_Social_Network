package com.example.lucky_social_network.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserStatisticsDTO {
    private Long userId;
    private String username;
    private String avatarUrl;

    private Long currentSessionHours;
    private Long currentSessionMinutes;

    private Long todayHours;
    private Long todayMinutes;
    private Integer todayLoginCount;
    private Integer todayActionsCount;

    private Long totalTimeHours;
    private Long totalTimeMinutes;
    private Long totalLogins;
    private Long totalActions;

    private LocalDateTime firstActivity;
    private LocalDateTime lastActivity;
    private LocalDateTime lastLoginTime;
}