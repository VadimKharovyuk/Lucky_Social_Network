package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.NotificationDto;
import com.example.lucky_social_network.service.NotificationService;
import com.example.lucky_social_network.service.PostService;
import com.example.lucky_social_network.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;
    private final PostService postService;

    @GetMapping
    public String getNotifications(Model model) {
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            return "redirect:/login";
        }

        List<NotificationDto> notificationDtos = notificationService.getUserNotificationsWithDetails(currentUserId);
        model.addAttribute("notifications", notificationDtos);

        long unreadCount = notificationService.getUnreadNotificationCount(currentUserId);
        model.addAttribute("notificationCount", unreadCount);

        return "notifications/notifications";
    }

    @PostMapping("/{notificationId}/read")
    public String markAsRead(@PathVariable Long notificationId) {
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            // Пользователь не аутентифицирован, перенаправляем на страницу входа
            return "redirect:/login";
        }

        try {
            notificationService.markAsRead(notificationId, currentUserId);
        } catch (EntityNotFoundException e) {
            // Уведомление не найдено
            return "redirect:/error";
        } catch (AccessDeniedException e) {
            // У пользователя нет прав для выполнения этого действия
            return "redirect:/error";
        }

        return "redirect:/notifications";
    }
}