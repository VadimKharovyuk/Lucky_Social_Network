package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }

        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findByUsername(username);

            // Обновляем время входа
            userService.updateLastLogin(user.getId());


            return "redirect:/posts";
        } catch (Exception e) {
            log.error("Login error for user {}: {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка входа в систему");
            return "redirect:/login";
        }
    }


    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("message", "Вы успешно вышли из системы");
        return "redirect:/login?logout";
    }
}