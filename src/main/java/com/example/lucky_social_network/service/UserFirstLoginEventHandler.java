package com.example.lucky_social_network.service;


import com.example.lucky_social_network.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Обработчик события первого входа
@Component
@Slf4j
@RequiredArgsConstructor
public class UserFirstLoginEventHandler {
    private final EmailService emailService;


    @EventListener
    public void handleUserFirstLogin(UserFirstLoginEvent event) {
        User user = event.getUser();
        log.info("Обработка первого входа пользователя: {}", user.getUsername());

        // Здесь можно добавить дополнительную логику для первого входа:
        // - Отправка приветственного email
        // - Создание начальных настроек
        // - Добавление стартового контента
        // - Установка начальных предпочтений
        // и т.д.
    }
}

