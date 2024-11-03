package com.example.lucky_social_network.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter

// Создайте событие для группы
public class GroupActivityEvent extends ApplicationEvent {
    private final Long groupId;
    private final String activityType;
    private final Long userId; // опционально - кто совершил действие

    public GroupActivityEvent(Object source, Long groupId, String activityType, Long userId) {
        super(source);
        this.groupId = groupId;
        this.activityType = activityType;
        this.userId = userId;
    }

    public enum GroupActivityType {
        POST_CREATED,
        MEMBER_JOINED,
        MEMBER_LEFT,
        GROUP_UPDATED,
        PHOTO_ADDED,
        EVENT_CREATED,
        DISCUSSION_STARTED
        // другие типы активности группы
    }
}
