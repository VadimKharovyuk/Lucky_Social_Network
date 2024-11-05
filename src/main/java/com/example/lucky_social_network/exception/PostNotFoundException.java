package com.example.lucky_social_network.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostNotFoundException extends RuntimeException {

    private final Long postId;

    public PostNotFoundException(Long postId) {
        super(String.format("Post not found with id: %d", postId));
        this.postId = postId;
    }

    public PostNotFoundException(String message, Long postId) {
        super(message);
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }
}
