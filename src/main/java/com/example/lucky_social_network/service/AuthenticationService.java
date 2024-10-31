package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Service
@Slf4j
public class AuthenticationService {
    private final UserActivityService userActivityService;
    private final UserSessionService userSessionService;

    @Autowired
    public AuthenticationService(UserActivityService userActivityService,
                                 UserSessionService userSessionService) {
        this.userActivityService = userActivityService;
        this.userSessionService = userSessionService;
    }

    public void handleLogin(User user) {
        userSessionService.startSession(user.getId());
        userActivityService.trackUserLogin(user);
        log.info("User {} logged in at {}", user.getUsername(), LocalDateTime.now());
    }

    public void handleLogout(User user) {
        LocalDateTime startTime = userSessionService.getSessionStartTime(user.getId());
        if (startTime != null) {
            long secondsSpent = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
            userActivityService.updateUserTimeSpent(user, secondsSpent);
            userSessionService.endSession(user.getId());
            log.info("User {} logged out. Session duration: {} seconds",
                    user.getUsername(), secondsSpent);
        }
    }

}