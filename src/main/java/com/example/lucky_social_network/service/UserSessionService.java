package com.example.lucky_social_network.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserSessionService {
    private final Map<Long, LocalDateTime> userSessionStartTimes = new ConcurrentHashMap<>();

    public void startSession(Long userId) {
        if (userId == null) {
            log.warn("Attempt to start session with null userId");
            return;
        }

        LocalDateTime previousSession = userSessionStartTimes.put(userId, LocalDateTime.now());
        if (previousSession != null) {
            log.warn("Overwriting existing session for user ID: {}. Previous session start: {}",
                    userId, previousSession);
        }
        log.info("Started new session for user ID: {} at {}", userId, LocalDateTime.now());
    }

    public void endSession(Long userId) {
        if (userId == null) {
            log.warn("Attempt to end session with null userId");
            return;
        }

        LocalDateTime sessionStart = userSessionStartTimes.remove(userId);
        if (sessionStart != null) {
            Duration sessionDuration = Duration.between(sessionStart, LocalDateTime.now());
            log.info("Ended session for user ID: {}. Session duration: {} minutes",
                    userId, sessionDuration.toMinutes());
        } else {
            log.warn("Attempt to end non-existent session for user ID: {}", userId);
        }
    }

    public LocalDateTime getSessionStartTime(Long userId) {
        if (userId == null) {
            log.warn("Attempt to get session start time with null userId");
            return null;
        }

        LocalDateTime startTime = userSessionStartTimes.get(userId);
        log.debug("Retrieved session start time for user ID: {}. Start time: {}",
                userId, startTime);
        return startTime;
    }

    public Duration getCurrentSessionDuration(Long userId) {
        if (userId == null) {
            log.warn("Attempt to get session duration with null userId");
            return null;
        }

        LocalDateTime startTime = userSessionStartTimes.get(userId);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            log.debug("Calculated session duration for user ID: {}. Duration: {} minutes",
                    userId, duration.toMinutes());
            return duration;
        }

        log.debug("No active session found for user ID: {}", userId);
        return null;
    }

    public boolean hasActiveSession(Long userId) {
        return userId != null && userSessionStartTimes.containsKey(userId);
    }

    public Optional<Duration> getSessionDurationSafe(Long userId) {
        return Optional.ofNullable(getCurrentSessionDuration(userId));
    }

    public int getActiveSessionsCount() {
        return userSessionStartTimes.size();
    }

    @Scheduled(fixedRate = 300000) // Каждые 5 минут
    public void cleanupInactiveSessions() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(24);
            int beforeSize = userSessionStartTimes.size();

            userSessionStartTimes.entrySet().removeIf(entry -> {
                boolean shouldRemove = entry.getValue().isBefore(threshold);
                if (shouldRemove) {
                    log.debug("Removing inactive session for user ID: {}. Start time: {}",
                            entry.getKey(), entry.getValue());
                }
                return shouldRemove;
            });

            int removedCount = beforeSize - userSessionStartTimes.size();
            if (removedCount > 0) {
                log.info("Cleaned up {} inactive sessions. Current active sessions: {}",
                        removedCount, userSessionStartTimes.size());
            }
        } catch (Exception e) {
            log.error("Error during inactive sessions cleanup: {}", e.getMessage(), e);
        }
    }

    // Метод для тестирования и администрирования
    public void terminateAllSessions() {
        int sessionCount = userSessionStartTimes.size();
        userSessionStartTimes.clear();
        log.warn("Terminated all {} active sessions", sessionCount);
    }
}