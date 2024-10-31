package com.example.lucky_social_network.config;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import com.example.lucky_social_network.service.AuthenticationService;
import com.example.lucky_social_network.service.UserActivityService;
import com.example.lucky_social_network.service.UserFirstLoginEvent;
import com.example.lucky_social_network.service.UserSessionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationService authenticationService;
    private final UserSessionService userSessionService;
    private final UserActivityService userActivityService;

    @Autowired
    public AuthenticationSuccessHandlerImpl(
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher,
            AuthenticationService authenticationService,
            UserSessionService userSessionService,
            UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.authenticationService = authenticationService;
        this.userSessionService = userSessionService;
        this.userActivityService = userActivityService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        log.info("Обработка успешной аутентификации для пользователя: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

            // Обновляем время последнего входа и проверяем первый вход
            processLoginTime(user);

            // Начинаем новую сессию
            startNewSession(user);

            response.sendRedirect("/posts");

            log.info("Успешная обработка входа пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при обработке входа пользователя {}: {}", username, e.getMessage(), e);
            response.sendRedirect("/login?error");
        }
    }

    private void processLoginTime(User user) {
        log.debug("Обработка времени входа для пользователя: {}", user.getUsername());

        boolean isFirstLogin = user.getLastLogin() == null;
        LocalDateTime previousLogin = user.getLastLogin();

        // Обновляем время последнего входа
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("Обновлено время входа для {}: предыдущее={}, новое={}",
                user.getUsername(), previousLogin, user.getLastLogin());

        // Обрабатываем первый вход
        if (isFirstLogin) {
            handleFirstLogin(user);
        }
    }

    private void handleFirstLogin(User user) {
        try {
            log.info("Обработка первого входа пользователя: {}", user.getUsername());
            eventPublisher.publishEvent(new UserFirstLoginEvent(this, user));
            log.debug("Событие первого входа опубликовано для: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Ошибка при публикации события первого входа для {}: {}",
                    user.getUsername(), e.getMessage());
        }
    }

    private void startNewSession(User user) {
        try {
            // Начинаем отслеживание сессии
            userSessionService.startSession(user.getId());

            // Обновляем статистику
            userActivityService.trackUserLogin(user);

            log.debug("Новая сессия начата для пользователя: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Ошибка при начале новой сессии для {}: {}",
                    user.getUsername(), e.getMessage());
            // Не прерываем процесс аутентификации при ошибке статистики
        }
    }
}


//package com.example.lucky_social_network.config;
//
//import com.example.lucky_social_network.model.User;
//import com.example.lucky_social_network.repository.UserRepository;
//import com.example.lucky_social_network.service.AuthenticationService;
//import com.example.lucky_social_network.service.UserFirstLoginEvent;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//
//@Slf4j
//@Component
//public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ApplicationEventPublisher eventPublisher;
//    @Autowired
//    private AuthenticationService authenticationService;
//
//
//    @Override
//    @Transactional
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        String username = authentication.getName();
//        log.info("Обработка успешной аутентификации для пользователя: {}", username);
//
//        try {
//            User user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
//
//            // Обновляем время входа
//            updateLoginTime(user);
//
//            // Обрабатываем статистику входа
//            handleLoginStatistics(user);
//
//            response.sendRedirect("/posts");
//        } catch (Exception e) {
//            log.error("Ошибка при обработке входа пользователя: {}", e.getMessage(), e);
//            response.sendRedirect("/login?error");
//        }
//    }
//
//    private void updateLoginTime(User user) {
//        log.info("Старое время входа для {}: {}", user.getUsername(), user.getLastLogin());
//
//        boolean isFirstLogin = user.getLastLogin() == null;
//        user.setLastLogin(LocalDateTime.now());
//        userRepository.save(user);
//
//        log.info("Обновлено время входа для {}: {}", user.getUsername(), user.getLastLogin());
//
//        // Проверяем сохранение
//        User updatedUser = userRepository.findByUsername(user.getUsername())
//                .orElseThrow(() -> new RuntimeException("Пользователь не найден после обновления"));
//        log.info("Проверка времени входа после сохранения: {}", updatedUser.getLastLogin());
//
//        if (isFirstLogin) {
//            log.info("Зафиксирован первый вход пользователя: {}", user.getUsername());
//            publishFirstLoginEvent(updatedUser);
//        }
//    }
//
//    private void publishFirstLoginEvent(User user) {
//        try {
//            eventPublisher.publishEvent(new UserFirstLoginEvent(this, user));
//            log.debug("Опубликовано событие первого входа для пользователя: {}",
//                    user.getUsername());
//        } catch (Exception e) {
//            log.error("Ошибка при публикации события первого входа: {}", e.getMessage());
//        }
//    }
//
//    private void handleLoginStatistics(User user) {
//        try {
//            // Начинаем отслеживание новой сессии
//            authenticationService.handleLogin(user);
//            log.debug("Статистика входа обработана для пользователя: {}", user.getUsername());
//        } catch (Exception e) {
//            log.error("Ошибка при обработке статистики входа: {}", e.getMessage());
//            // Не прерываем основной процесс аутентификации при ошибке статистики
//        }
//    }
//}