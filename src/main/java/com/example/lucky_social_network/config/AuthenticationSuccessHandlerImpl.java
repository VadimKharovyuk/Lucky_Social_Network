package com.example.lucky_social_network.config;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

            log.info("Старое время входа для {}: {}", username, user.getLastLogin());

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("Обновлено время входа для {}: {}", username, user.getLastLogin());

            // Проверяем сохранение
            User updatedUser = userRepository.findByUsername(username).orElseThrow();
            log.info("Проверка времени входа после сохранения: {}", updatedUser.getLastLogin());

            response.sendRedirect("/posts");
        } catch (Exception e) {
            log.error("Ошибка при обновлении времени входа: {}", e.getMessage(), e);
            response.sendRedirect("/login?error");
        }
    }
}
