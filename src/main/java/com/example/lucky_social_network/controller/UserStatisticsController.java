package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserDailyActivity;
import com.example.lucky_social_network.service.AuthenticationService;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.UserActivityService;
import com.example.lucky_social_network.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user/statistics")
@Slf4j
public class UserStatisticsController {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String getUserStatistics(Model model) {
        try {
            // Получаем CustomUserDetails
            Object principal = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            // Проверяем тип и получаем User
            User currentUser;
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                currentUser = userService.findById(userDetails.getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                throw new RuntimeException("Invalid authentication principal");
            }

            // Остальной код остается без изменений...
            LocalDateTime sessionStart = authenticationService.getSessionStartTime(currentUser.getId());
            if (sessionStart != null) {
                Duration currentSession = Duration.between(sessionStart, LocalDateTime.now());
                model.addAttribute("currentSessionHours", currentSession.toHours());
                model.addAttribute("currentSessionMinutes", currentSession.toMinutesPart());
            }

            // Получаем статистику за сегодня
            LocalDate today = LocalDate.now();
            UserDailyActivity todayActivity = userActivityService.getUserActivityForDate(currentUser, today);
            if (todayActivity != null) {
                model.addAttribute("todayLoginCount", todayActivity.getLoginCount());
                model.addAttribute("todayActionsCount", todayActivity.getActionsCount());

                // Форматируем общее время
                long totalSeconds = todayActivity.getTimeSpentSeconds();
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                model.addAttribute("todayHours", hours);
                model.addAttribute("todayMinutes", minutes);
            }

            // Получаем общую статистику за все время
            List<UserDailyActivity> allActivities = userActivityService.getAllUserActivities(currentUser);
            long totalTimeSeconds = allActivities.stream()
                    .mapToLong(UserDailyActivity::getTimeSpentSeconds)
                    .sum();
            long totalActions = allActivities.stream()
                    .mapToLong(UserDailyActivity::getActionsCount)
                    .sum();
            long totalLogins = allActivities.stream()
                    .mapToLong(UserDailyActivity::getLoginCount)
                    .sum();

            model.addAttribute("totalTimeHours", totalTimeSeconds / 3600);
            model.addAttribute("totalTimeMinutes", (totalTimeSeconds % 3600) / 60);
            model.addAttribute("totalActions", totalActions);
            model.addAttribute("totalLogins", totalLogins);

            // Добавляем информацию о пользователе
            model.addAttribute("user", currentUser);
            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("avatar", currentUser.getAvatarUrl());
            model.addAttribute("lastLoginTime",
                    todayActivity != null ? todayActivity.getLastLoginTime() : null);

            return "user/statistics";
        } catch (Exception e) {
            log.error("Error getting user statistics: {}", e.getMessage());
            model.addAttribute("error", "Произошла ошибка при загрузке статистики");
            return "error";
        }
    }

}