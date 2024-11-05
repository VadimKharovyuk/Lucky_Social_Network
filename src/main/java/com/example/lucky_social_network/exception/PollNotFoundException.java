package com.example.lucky_social_network.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND) // Добавляем HTTP статус
public class PollNotFoundException extends RuntimeException {

    private final Long pollId;

    public PollNotFoundException(Long pollId) {
        super(String.format("Poll with id %d not found", pollId));
        this.pollId = pollId;
    }

    public Long getPollId() {
        return pollId;
    }
}
