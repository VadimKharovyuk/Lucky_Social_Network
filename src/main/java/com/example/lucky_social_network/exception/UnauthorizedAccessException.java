package com.example.lucky_social_network.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    // Для передачи дополнительных деталей
    private String details;
    private String action;
    private Long targetId;

    // Конструктор с дополнительными деталями
    public UnauthorizedAccessException(String message, String action, Long targetId) {
        super(message);
        this.action = action;
        this.targetId = targetId;
    }

    // Конструктор с полными деталями
    public UnauthorizedAccessException(String message, String details, String action, Long targetId) {
        super(message);
        this.details = details;
        this.action = action;
        this.targetId = targetId;
    }

    // Геттеры для дополнительной информации
    public String getDetails() {
        return details;
    }

    public String getAction() {
        return action;
    }

    public Long getTargetId() {
        return targetId;
    }
}