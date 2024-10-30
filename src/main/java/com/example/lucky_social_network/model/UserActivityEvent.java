package com.example.lucky_social_network.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserActivityEvent extends ApplicationEvent {
    private final Long userId;
    private final String activityType;

    public UserActivityEvent(Object source, Long userId, String activityType) {
        super(source);
        this.userId = userId;
        this.activityType = activityType;
    }

    public enum ActivityType {
        POST_CREATED,
        COMMENT_ADDED,
        LIKE_ADDED,
        PROFILE_UPDATED,
        PHOTO_UPLOADED,
        MESSAGE_SENT
    }
}
