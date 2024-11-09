package com.example.lucky_social_network.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AdNotFoundException extends RuntimeException {

    public AdNotFoundException(Long id) {
        super(String.format("Advertisement with id %d not found", id));
    }

    public AdNotFoundException(String message) {
        super(message);
    }

    public AdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
