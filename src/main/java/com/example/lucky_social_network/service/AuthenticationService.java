package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AuthenticationService {

    @Autowired
    private UserActivityService userActivityService;

    // Храним время начала сессии для каждого пользователя
    private final Map<Long, LocalDateTime> userSessionStartTimes = new ConcurrentHashMap<>();

    public void handleLogin(User user) {
        // Сохраняем время начала сессии
        userSessionStartTimes.put(user.getId(), LocalDateTime.now());

        // Отмечаем вход в статистике
        userActivityService.trackUserLogin(user);

        log.info("User {} logged in at {}", user.getUsername(), LocalDateTime.now());
    }

    public void handleLogout(User user) {
        // Получаем время начала сессии
        LocalDateTime startTime = userSessionStartTimes.get(user.getId());

        if (startTime != null) {
            // Вычисляем продолжительность сессии
            long secondsSpent = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());

            // Обновляем статистику
            userActivityService.updateUserTimeSpent(user, secondsSpent);

            // Удаляем время начала сессии
            userSessionStartTimes.remove(user.getId());

            log.info("User {} logged out. Session duration: {} seconds",
                    user.getUsername(), secondsSpent);
        }
    }

    public LocalDateTime getSessionStartTime(Long userId) {
        return userSessionStartTimes.get(userId);
    }

    // Дополнительный метод для получения продолжительности текущей сессии
    public Duration getCurrentSessionDuration(Long userId) {
        LocalDateTime startTime = userSessionStartTimes.get(userId);
        if (startTime != null) {
            return Duration.between(startTime, LocalDateTime.now());
        }
        return null;
    }

    // Метод для очистки неактивных сессий
    @Scheduled(fixedRate = 300000) // Каждые 5 минут
    public void cleanupInactiveSessions() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(24); // Очищаем сессии старше 24 часов
            userSessionStartTimes.entrySet().removeIf(entry ->
                    entry.getValue().isBefore(threshold));
        } catch (Exception e) {
            log.error("Error cleaning up inactive sessions: {}", e.getMessage());
        }
    }
}