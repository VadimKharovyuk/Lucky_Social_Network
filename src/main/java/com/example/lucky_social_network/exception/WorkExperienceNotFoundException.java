package com.example.lucky_social_network.exception;

public class WorkExperienceNotFoundException extends RuntimeException {
    public WorkExperienceNotFoundException(Long id) {
        super("Work experience not found with id: " + id);
    }

    public WorkExperienceNotFoundException(String message) {
        super(message);
    }

}
