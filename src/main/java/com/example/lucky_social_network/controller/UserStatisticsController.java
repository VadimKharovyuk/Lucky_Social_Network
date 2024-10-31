package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.UserStatisticsDTO;
import com.example.lucky_social_network.model.User;
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

@Controller
@RequestMapping("/user/statistics")
@Slf4j
public class UserStatisticsController {

    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private UserService userService;


    @GetMapping
    public String getUserStatistics(Model model) {
        try {
            // Получаем пользователя
            Object principal = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            if (!(principal instanceof CustomUserDetails)) {
                throw new RuntimeException("Invalid authentication principal");
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;
            User currentUser = userService.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Получаем статистику
            UserStatisticsDTO statistics = userActivityService.getUserStatistics(currentUser);

            // Добавляем все данные в модель
            model.addAttribute("user", currentUser);
            model.addAttribute("username", statistics.getUsername());
            model.addAttribute("avatar", statistics.getAvatarUrl());

            model.addAttribute("currentSessionHours", statistics.getCurrentSessionHours());
            model.addAttribute("currentSessionMinutes", statistics.getCurrentSessionMinutes());

            model.addAttribute("todayHours", statistics.getTodayHours());
            model.addAttribute("todayMinutes", statistics.getTodayMinutes());
            model.addAttribute("todayLoginCount", statistics.getTodayLoginCount());
            model.addAttribute("todayActionsCount", statistics.getTodayActionsCount());

            model.addAttribute("totalTimeHours", statistics.getTotalTimeHours());
            model.addAttribute("totalTimeMinutes", statistics.getTotalTimeMinutes());
            model.addAttribute("totalLogins", statistics.getTotalLogins());
            model.addAttribute("totalActions", statistics.getTotalActions());

            model.addAttribute("firstActivity", statistics.getFirstActivity());
            model.addAttribute("lastActivity", statistics.getLastActivity());
            model.addAttribute("lastLoginTime", statistics.getLastLoginTime());

            return "user/statistics";
        } catch (Exception e) {
            log.error("Error getting user statistics: {}", e.getMessage());
            model.addAttribute("error", "Произошла ошибка при загрузке статистики");
            return "error";
        }
    }

}