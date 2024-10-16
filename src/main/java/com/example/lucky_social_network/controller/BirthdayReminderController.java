package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.UserBirthdayDto;
import com.example.lucky_social_network.service.BirthdayReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/birthday-reminders")
@RequiredArgsConstructor
public class BirthdayReminderController {

    private final BirthdayReminderService birthdayReminderService;

    @PostMapping("/send-reminders")
    public ResponseEntity<String> sendReminders() {
        birthdayReminderService.sendBirthdayReminders();
        return ResponseEntity.ok("Birthday reminders sent successfully");
    }

    @PostMapping("/send-reminder/{userId}")
    public ResponseEntity<String> sendReminderForUser(@PathVariable Long userId) {
        boolean sent = birthdayReminderService.sendReminderForUser(userId);
        if (sent) {
            return ResponseEntity.ok("Birthday reminder sent for user " + userId);
        } else {
            return ResponseEntity.badRequest().body("Failed to send birthday reminder for user " + userId);
        }
    }

    @GetMapping("/next-birthdays")
    public ResponseEntity<List<UserBirthdayDto>> getNextBirthdays(@RequestParam(defaultValue = "7") int days) {
        List<UserBirthdayDto> upcomingBirthdays = birthdayReminderService.getUpcomingBirthdays(days);
        log.info("Returning {} upcoming birthdays", upcomingBirthdays.size());
        if (upcomingBirthdays.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(upcomingBirthdays);
    }
}