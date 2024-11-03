package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.GroupActivityEvent;
import com.example.lucky_social_network.model.UserActivityEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityPublisher {


    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishActivity(Long userId, UserActivityEvent.ActivityType activityType) {
        log.debug("Публикация события активности: {} для пользователя: {}",
                activityType, userId);
        UserActivityEvent event = new UserActivityEvent(this, userId, activityType.name());
        eventPublisher.publishEvent(event);
    }
    
    public void publishGroupActivity(Long groupId, GroupActivityEvent.GroupActivityType activityType) {
        log.debug("Публикация события активности группы: {} для группы: {}",
                activityType, groupId);
        GroupActivityEvent event = new GroupActivityEvent(this, groupId, activityType.name(), null);
        eventPublisher.publishEvent(event);
    }

    // Публикация события группы с userId
    public void publishGroupActivity(Long groupId, GroupActivityEvent.GroupActivityType activityType, Long userId) {
        log.debug("Публикация события активности группы: {} для группы: {} от пользователя: {}",
                activityType, groupId, userId);
        GroupActivityEvent event = new GroupActivityEvent(this, groupId, activityType.name(), userId);
        eventPublisher.publishEvent(event);
    }

}
