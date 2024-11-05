package com.example.lucky_social_network.exception;

import com.example.lucky_social_network.model.Poll;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)  // HTTP статус 400
public class PollNotActiveException extends RuntimeException {

    private final Long pollId;
    private final Poll.PollStatus currentStatus;

    public PollNotActiveException(Long pollId, Poll.PollStatus currentStatus) {
        super(String.format("Poll with id %d is not active. Current status: %s", pollId, currentStatus));
        this.pollId = pollId;
        this.currentStatus = currentStatus;
    }

    public PollNotActiveException(Long pollId) {
        super(String.format("Poll with id %d is not active", pollId));
        this.pollId = pollId;
        this.currentStatus = null;
    }

    public Long getPollId() {
        return pollId;
    }

    public Poll.PollStatus getCurrentStatus() {
        return currentStatus;
    }
}
