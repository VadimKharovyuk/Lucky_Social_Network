package com.example.lucky_social_network.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class TimeUtils {

    public static String getTimeAgo(LocalDateTime dateTime) {
        log.info("Получение форматированного времени для: {}", dateTime);

        if (dateTime == null) {
            log.debug("Получено null значение времени");
            return "никогда не был в сети";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        log.info("Разница во времени: {} минут", duration.toMinutes());

        if (duration.toMinutes() < 1) return "только что";
        if (duration.toMinutes() < 60) {
            long minutes = duration.toMinutes();
            return minutes + " " + getMinutesForm(minutes);
        }
        if (duration.toHours() < 24) {
            long hours = duration.toHours();
            return hours + " " + getHoursForm(hours);
        }
        if (duration.toDays() < 7) {
            long days = duration.toDays();
            return days + " " + getDaysForm(days);
        }

        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    private static String getMinutesForm(long minutes) {
        if (minutes % 10 == 1 && minutes % 100 != 11) return "минуту назад";
        if (minutes % 10 >= 2 && minutes % 10 <= 4 && (minutes % 100 < 10 || minutes % 100 >= 20))
            return "минуты назад";
        return "минут назад";
    }

    private static String getHoursForm(long hours) {
        if (hours % 10 == 1 && hours % 100 != 11) return "час назад";
        if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20))
            return "часа назад";
        return "часов назад";
    }

    private static String getDaysForm(long days) {
        if (days % 10 == 1 && days % 100 != 11) return "день назад";
        if (days % 10 >= 2 && days % 10 <= 4 && (days % 100 < 10 || days % 100 >= 20))
            return "дня назад";
        return "дней назад";
    }
}