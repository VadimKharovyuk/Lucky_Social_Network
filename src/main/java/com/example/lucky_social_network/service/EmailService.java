package com.example.lucky_social_network.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final AtomicInteger emailCount = new AtomicInteger(0);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    private static final int MAX_EMAILS_PER_SECOND = 10;
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String text) {
        if (!checkRateLimit()) {
            log.warn("Rate limit exceeded for email sending");
            return CompletableFuture.completedFuture(false);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        long previousResetTime = lastResetTime.get();

        if (currentTime - previousResetTime >= ONE_SECOND_IN_MILLIS) {
            lastResetTime.set(currentTime);
            emailCount.set(1);
            return true;
        }

        return emailCount.incrementAndGet() <= MAX_EMAILS_PER_SECOND;
    }
}