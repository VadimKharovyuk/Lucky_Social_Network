package com.example.lucky_social_network.service;

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
}
