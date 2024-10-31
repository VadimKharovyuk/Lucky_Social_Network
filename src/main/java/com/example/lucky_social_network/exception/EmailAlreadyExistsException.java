package com.example.lucky_social_network.exception;

// Создаем класс исключения
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
