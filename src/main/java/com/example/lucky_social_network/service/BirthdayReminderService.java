package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.UserBirthdayDto;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class BirthdayReminderService {
    private final UserRepository userRepository;

    private final EmailService emailService;

//    @Scheduled(cron = "0 0 0 * * *") // Запускается каждый день в полночь
//    public void sendBirthdayReminders() {
//        LocalDate today = LocalDate.now();
//        List<User> users = userRepository.findAll();
//
//        for (User user : users) {
//            if (user.getDateOfBirth() != null) {
//                LocalDate birthday = user.getDateOfBirth().withYear(today.getYear());
//                long daysUntilBirthday = ChronoUnit.DAYS.between(today, birthday);
//
//                if (daysUntilBirthday >= 0 && daysUntilBirthday <= 5) {
//                    sendBirthdayReminder(user, daysUntilBirthday);
//                }
//            }
//        }
//    }

    private void sendBirthdayReminder(User user, long daysUntilBirthday) {
        String subject = "Birthday Reminder";
        String message;

        if (daysUntilBirthday == 0) {
            message = "Happy Birthday, " + user.getUsername() + "!";
        } else {
            message = "Your birthday is coming up in " + daysUntilBirthday + " days!";
        }

        emailService.sendEmail(user.getEmail(), subject, message);
    }

    public boolean sendReminderForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getDateOfBirth() != null) {
            LocalDate today = LocalDate.now();
            LocalDate birthday = user.getDateOfBirth().withYear(today.getYear());
            long daysUntilBirthday = ChronoUnit.DAYS.between(today, birthday);
            sendBirthdayReminder(user, daysUntilBirthday);
            return true;
        }
        return false;
    }

    public List<UserBirthdayDto> getUpcomingBirthdays(int days) {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();
        log.info("Found {} users in total", users.size());

        List<UserBirthdayDto> upcomingBirthdays = users.stream()
                .filter(user -> user.getDateOfBirth() != null)
                .map(user -> {
                    LocalDate birthday = user.getDateOfBirth().withYear(today.getYear());
                    if (birthday.isBefore(today)) {
                        birthday = birthday.plusYears(1);
                    }
                    long daysUntil = ChronoUnit.DAYS.between(today, birthday);
                    if (daysUntil <= days) {
                        return new UserBirthdayDto(user.getId(), user.getUsername(), birthday, daysUntil);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(UserBirthdayDto::getDaysUntil))
                .collect(Collectors.toList());

        log.info("Found {} upcoming birthdays in the next {} days", upcomingBirthdays.size(), days);
        upcomingBirthdays.forEach(dto -> log.info("Upcoming birthday: {}", dto));

        return upcomingBirthdays;
    }
}