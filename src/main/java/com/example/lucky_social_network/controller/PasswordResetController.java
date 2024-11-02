package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        log.info("Showing forgot password form");
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        log.info("Processing password reset request for email: {}", email);

        try {
            passwordResetService.resetPassword(email);
            log.info("Password reset successful for email: {}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Инструкции по сбросу пароля отправлены на вашу почту");
            return "redirect:/auth/forgot-password";
        } catch (Exception e) {
            log.error("Error in password reset process for email: {}", email, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при сбросе пароля. Пожалуйста, попробуйте позже");
            return "redirect:/auth/forgot-password";
        }
    }
}