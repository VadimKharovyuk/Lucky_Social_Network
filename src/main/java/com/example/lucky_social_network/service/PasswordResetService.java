package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int PASSWORD_LENGTH = 12;
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    @Transactional
    public void resetPassword(String email) {
        log.info("Starting password reset process for email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

            String newPassword = generateSecurePassword();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            sendPasswordResetEmail(user.getEmail(), newPassword)
                    .thenAccept(success -> {
                        if (success) {
                            log.info("Password reset email sent successfully to: {}", email);
                        } else {
                            log.error("Failed to send password reset email to: {}", email);
                        }
                    });

            log.info("Password reset completed successfully for email: {}", email);
        } catch (Exception e) {
            log.error("Error during password reset: ", e);
            throw e;
        }
    }

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARS.length());
            password.append(CHARS.charAt(randomIndex));
        }

        return password.toString();
    }

    private CompletableFuture<Boolean> sendPasswordResetEmail(String email, String newPassword) {
        String subject = "Сброс пароля";
        String text = String.format("""
                Ваш новый пароль: %s
                
                Пожалуйста, измените этот пароль после входа в систему в целях безопасности.
                
                Если вы не запрашивали сброс пароля, немедленно свяжитесь с поддержкой.
                """, newPassword);

        return emailService.sendEmail(email, subject, text);
    }
}