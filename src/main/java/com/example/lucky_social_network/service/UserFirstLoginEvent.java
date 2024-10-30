package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class UserFirstLoginEvent extends ApplicationEvent {
    private final User user;

    public UserFirstLoginEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
