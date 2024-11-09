package com.example.lucky_social_network.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Long userId) {
        super(String.format("User with id %d not found", userId));
    }
}