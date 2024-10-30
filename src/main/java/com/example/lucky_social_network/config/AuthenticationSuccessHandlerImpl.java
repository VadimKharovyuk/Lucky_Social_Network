package com.example.lucky_social_network.config;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import com.example.lucky_social_network.service.UserFirstLoginEvent;
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

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        log.info("Обработка успешной аутентификации для пользователя: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

            updateLoginTime(user);

            response.sendRedirect("/posts");
        } catch (Exception e) {
            log.error("Ошибка при обновлении времени входа: {}", e.getMessage(), e);
            response.sendRedirect("/login?error");
        }
    }

    private void updateLoginTime(User user) {
        log.info("Старое время входа для {}: {}", user.getUsername(), user.getLastLogin());

        boolean isFirstLogin = user.getLastLogin() == null;
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("Обновлено время входа для {}: {}", user.getUsername(), user.getLastLogin());

        // Проверяем сохранение
        User updatedUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден после обновления"));
        log.info("Проверка времени входа после сохранения: {}", updatedUser.getLastLogin());

        if (isFirstLogin) {
            log.info("Зафиксирован первый вход пользователя: {}", user.getUsername());
            // Здесь можно опубликовать событие первого входа, если нужно
            publishFirstLoginEvent(updatedUser);

        }
    }


    private void publishFirstLoginEvent(User user) {
        try {
            eventPublisher.publishEvent(new UserFirstLoginEvent(this, user));
            log.debug("Опубликовано событие первого входа для пользователя: {}",
                    user.getUsername());
        } catch (Exception e) {
            log.error("Ошибка при публикации события первого входа: {}", e.getMessage());
        }
    }
}
