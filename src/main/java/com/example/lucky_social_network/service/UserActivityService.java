package com.example.lucky_social_network.service;


import com.example.lucky_social_network.dto.UserStatisticsDTO;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserDailyActivity;
import com.example.lucky_social_network.repository.UserDailyActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserActivityService {

    private final UserDailyActivityRepository activityRepository;
    private final UserSessionService userSessionService;

    @Autowired
    public UserActivityService(UserDailyActivityRepository activityRepository,
                               UserSessionService userSessionService) {
        this.activityRepository = activityRepository;
        this.userSessionService = userSessionService;
    }


    @Transactional
    public void updateUserTimeSpent(User user, long seconds) {
        try {
            LocalDate today = LocalDate.now();
            UserDailyActivity activity = activityRepository.findByUserAndDate(user, today)
                    .orElseGet(() -> {
                        UserDailyActivity newActivity = new UserDailyActivity(user, today);
                        newActivity.setFirstActivity(LocalDateTime.now());
                        return newActivity;
                    });

            // Обновляем время
            activity.updateTimeSpent(seconds);

            // Обновляем время последней активности
            activity.setLastActivity(LocalDateTime.now());

            // Вычисляем общее время за сегодня
            if (activity.getFirstActivity() != null && activity.getLastActivity() != null) {
                long totalSeconds = Duration.between(activity.getFirstActivity(),
                        activity.getLastActivity()).getSeconds();
                activity.setTimeSpentSeconds(totalSeconds);
                log.debug("Updated total time spent for user {}: {} seconds",
                        user.getUsername(), totalSeconds);
            }

            UserDailyActivity savedActivity = activityRepository.save(activity);
            log.debug("Saved activity with time spent: {} seconds", savedActivity.getTimeSpentSeconds());
        } catch (Exception e) {
            log.error("Error updating time spent for user {}: {}", user.getUsername(), e.getMessage(), e);
        }
    }

    @Transactional
    public void trackUserAction(User user) {
        try {
            LocalDate today = LocalDate.now();
            UserDailyActivity activity = activityRepository.findByUserAndDate(user, today)
                    .orElseGet(() -> {
                        UserDailyActivity newActivity = new UserDailyActivity(user, today);
                        newActivity.setFirstActivity(LocalDateTime.now());
                        return newActivity;
                    });

            // Увеличиваем счетчик действий
            activity.incrementActionsCount();

            // Обновляем время последней активности
            LocalDateTime now = LocalDateTime.now();
            activity.setLastActivity(now);

            // Обновляем общее время
            if (activity.getFirstActivity() != null) {
                long totalSeconds = Duration.between(activity.getFirstActivity(), now).getSeconds();
                activity.setTimeSpentSeconds(totalSeconds);
            }

            UserDailyActivity savedActivity = activityRepository.save(activity);
            log.debug("Updated user activity: actions={}, time spent={}s",
                    savedActivity.getActionsCount(),
                    savedActivity.getTimeSpentSeconds());
        } catch (Exception e) {
            log.error("Error tracking action for user {}: {}", user.getUsername(), e.getMessage(), e);
        }
    }

    private UserStatisticsDTO buildStatisticsDTO(User user, Duration currentSession,
                                                 UserDailyActivity todayActivity, long totalTimeSeconds,
                                                 long totalActions, long totalLogins) {

        // Получаем текущее время активности если есть сессия
        if (currentSession != null && todayActivity != null &&
                todayActivity.getFirstActivity() != null) {

            // Добавляем время текущей сессии к общему времени
            totalTimeSeconds += currentSession.getSeconds();
            log.debug("Added current session time: {}s, new total: {}s",
                    currentSession.getSeconds(), totalTimeSeconds);
        }

        UserStatisticsDTO.UserStatisticsDTOBuilder builder = UserStatisticsDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .totalTimeHours(totalTimeSeconds / 3600)
                .totalTimeMinutes((totalTimeSeconds % 3600) / 60)
                .totalLogins(totalLogins)
                .totalActions(totalActions);

        if (currentSession != null) {
            builder.currentSessionHours((long) currentSession.toHours())
                    .currentSessionMinutes((long) currentSession.toMinutesPart());
        } else {
            builder.currentSessionHours(0L)
                    .currentSessionMinutes(0L);
        }

        if (todayActivity != null) {
            long todaySeconds = todayActivity.getTimeSpentSeconds();
            if (todayActivity.getFirstActivity() != null && todayActivity.getLastActivity() != null) {
                // Пересчитываем время за сегодня
                todaySeconds = Duration.between(todayActivity.getFirstActivity(),
                        todayActivity.getLastActivity()).getSeconds();
            }

            builder.todayHours(todaySeconds / 3600)
                    .todayMinutes((todaySeconds % 3600) / 60)
                    .todayLoginCount(todayActivity.getLoginCount())
                    .todayActionsCount(todayActivity.getActionsCount())
                    .lastLoginTime(todayActivity.getLastLoginTime())
                    .firstActivity(todayActivity.getFirstActivity())
                    .lastActivity(todayActivity.getLastActivity());
        } else {
            builder.todayHours(0L)
                    .todayMinutes(0L)
                    .todayLoginCount(0)
                    .todayActionsCount(0);
        }

        UserStatisticsDTO statistics = builder.build();
        log.info("Generated statistics for user {}: [total time={}h {}m, actions={}, logins={}]",
                user.getUsername(),
                statistics.getTotalTimeHours(),
                statistics.getTotalTimeMinutes(),
                statistics.getTotalActions(),
                statistics.getTotalLogins());

        return statistics;
    }


    @Transactional
    public void trackUserLogin(User user) {
        try {
            LocalDate today = LocalDate.now();
            UserDailyActivity activity = activityRepository.findByUserAndDate(user, today)
                    .orElseGet(() -> {
                        UserDailyActivity newActivity = new UserDailyActivity(user, today);
                        newActivity.setFirstActivity(LocalDateTime.now());
                        return newActivity;
                    });

            activity.incrementLoginCount();
            activity.setLastLoginTime(LocalDateTime.now());

            // Обновляем время активности
            updateActivityTimes(activity);

            UserDailyActivity savedActivity = activityRepository.save(activity);
            log.debug("Tracked login for user {} at {}. Login count: {}",
                    user.getUsername(), LocalDateTime.now(), savedActivity.getLoginCount());
        } catch (Exception e) {
            log.error("Error tracking login for user {}: {}", user.getUsername(), e.getMessage(), e);
        }
    }


    private void updateActivityTimes(UserDailyActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        if (activity.getFirstActivity() == null) {
            activity.setFirstActivity(now);
        }
        activity.setLastActivity(now);
    }

    public UserStatisticsDTO getUserStatistics(User user) {
        try {
            log.debug("Starting statistics calculation for user: {}", user.getUsername());

            // Получаем текущую сессию
            Duration currentSession = userSessionService.getCurrentSessionDuration(user.getId());
            if (currentSession != null) {
                log.debug("Current session for user {}: {}m {}s",
                        user.getUsername(),
                        currentSession.toMinutes(),
                        currentSession.toSecondsPart());
            }

            // Получаем статистику за сегодня
            LocalDate today = LocalDate.now();
            UserDailyActivity todayActivity = getUserActivityForDate(user, today);
            logTodayActivity(user, todayActivity);

            // Получаем общую статистику
            List<UserDailyActivity> allActivities = getAllUserActivities(user);
            long[] totals = calculateTotals(user, allActivities);
            long totalTimeSeconds = totals[0];
            long totalActions = totals[1];
            long totalLogins = totals[2];

            return buildStatisticsDTO(user, currentSession, todayActivity,
                    totalTimeSeconds, totalActions, totalLogins);

        } catch (Exception e) {
            log.error("Error calculating statistics for user {}: {}",
                    user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error calculating user statistics", e);
        }
    }


    private long[] calculateTotals(User user, List<UserDailyActivity> activities) {
        long totalTimeSeconds = 0;
        long totalActions = 0;
        long totalLogins = 0;

        for (UserDailyActivity activity : activities) {
            totalTimeSeconds += activity.getTimeSpentSeconds();
            totalActions += activity.getActionsCount();
            totalLogins += activity.getLoginCount();

            log.debug("Activity record for {}: date={}, actions={}, logins={}, timeSpent={}s",
                    user.getUsername(),
                    activity.getDate(),
                    activity.getActionsCount(),
                    activity.getLoginCount(),
                    activity.getTimeSpentSeconds());
        }

        log.debug("Calculated totals for user {}: [timeSpent={}s, actions={}, logins={}]",
                user.getUsername(), totalTimeSeconds, totalActions, totalLogins);

        return new long[]{totalTimeSeconds, totalActions, totalLogins};
    }


    public UserDailyActivity getUserActivityForDate(User user, LocalDate date) {
        try {
            log.debug("Getting activity for user {} on date {}", user.getUsername(), date);

            UserDailyActivity activity = activityRepository.findByUserAndDate(user, date)
                    .orElse(null);

            if (activity != null) {
                log.debug("Found activity for user {} on {}: [actions={}, logins={}, timeSpent={}s, firstActivity={}, lastActivity={}]",
                        user.getUsername(),
                        date,
                        activity.getActionsCount(),
                        activity.getLoginCount(),
                        activity.getTimeSpentSeconds(),
                        activity.getFirstActivity(),
                        activity.getLastActivity());
            } else {
                log.debug("No activity found for user {} on date {}", user.getUsername(), date);
            }

            return activity;
        } catch (Exception e) {
            log.error("Error getting activity for user {} on date {}: {}",
                    user.getUsername(), date, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получает все записи активности пользователя, отсортированные по дате
     */
    public List<UserDailyActivity> getAllUserActivities(User user) {
        try {
            log.debug("Getting all activities for user {}", user.getUsername());

            List<UserDailyActivity> activities = activityRepository.findByUserOrderByDateDesc(user);

            if (!activities.isEmpty()) {
                log.debug("Found {} activity records for user {}", activities.size(), user.getUsername());
                activities.forEach(activity ->
                        log.debug("Activity record: [date={}, actions={}, logins={}, timeSpent={}s, firstActivity={}, lastActivity={}]",
                                activity.getDate(),
                                activity.getActionsCount(),
                                activity.getLoginCount(),
                                activity.getTimeSpentSeconds(),
                                activity.getFirstActivity(),
                                activity.getLastActivity())
                );
            } else {
                log.debug("No activity records found for user {}", user.getUsername());
            }

            return activities;
        } catch (Exception e) {
            log.error("Error getting all activities for user {}: {}",
                    user.getUsername(), e.getMessage(), e);
            return List.of(); // Возвращаем пустой список в случае ошибки
        }
    }

    /**
     * Логирует информацию о сегодняшней активности пользователя
     */
    private void logTodayActivity(User user, UserDailyActivity activity) {
        if (activity != null) {
            log.info("Today's activity summary for user {}:", user.getUsername());
            log.info("- Actions count: {}", activity.getActionsCount());
            log.info("- Logins count: {}", activity.getLoginCount());
            log.info("- Time spent: {}s", activity.getTimeSpentSeconds());
            log.info("- First activity: {}",
                    activity.getFirstActivity() != null ? activity.getFirstActivity() : "N/A");
            log.info("- Last activity: {}",
                    activity.getLastActivity() != null ? activity.getLastActivity() : "N/A");
            log.info("- Last login: {}",
                    activity.getLastLoginTime() != null ? activity.getLastLoginTime() : "N/A");
        } else {
            log.info("No activity recorded today for user {}", user.getUsername());
        }
    }

    /**
     * Форматирует время активности для логов
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, remainingSeconds);
    }

}
