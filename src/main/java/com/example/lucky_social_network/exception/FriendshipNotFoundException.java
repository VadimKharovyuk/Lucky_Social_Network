package com.example.lucky_social_network.exception;

public class FriendshipNotFoundException extends RuntimeException {

    public FriendshipNotFoundException(String message) {
        super(message);
    }

    public FriendshipNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FriendshipNotFoundException(Long userId, Long friendId) {
        super(String.format("Дружба не найдена между пользователями с ID %d и %d", userId, friendId));
    }
}
