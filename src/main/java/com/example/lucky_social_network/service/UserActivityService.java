package com.example.lucky_social_network.service;


import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserDailyActivity;
import com.example.lucky_social_network.repository.UserDailyActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class UserActivityService {

    @Autowired
    private UserDailyActivityRepository activityRepository;

    @Transactional
    public void trackUserLogin(User user) {
        LocalDate today = LocalDate.now();
        UserDailyActivity activity = activityRepository.findByUserAndDate(user, today)
                .orElseGet(() -> new UserDailyActivity(user, today));

        activity.incrementLoginCount();
        activityRepository.save(activity);
    }

    @Transactional
    public void trackUserAction(User user) {
        LocalDate today = LocalDate.now();
        UserDailyActivity activity = activityRepository.findByUserAndDate(user, today)
                .orElseGet(() -> new UserDailyActivity(user, today));

        activity.incrementActionsCount();
        activityRepository.save(activity);
    }

    public void updateUserTimeSpent(User user, long seconds) {
        LocalDate today = LocalDate.now();
        UserDailyActivity activity = activityRepository.findByUserAndDate(user, today)
                .orElseGet(() -> new UserDailyActivity(user, today));

        activity.updateTimeSpent(seconds);
        activityRepository.save(activity);
    }

    public UserDailyActivity getUserActivityForDate(User user, LocalDate date) {
        return activityRepository.findByUserAndDate(user, date).orElse(null);
    }

    public List<UserDailyActivity> getAllUserActivities(User user) {
        return activityRepository.findByUserOrderByDateDesc(user);
    }
}
