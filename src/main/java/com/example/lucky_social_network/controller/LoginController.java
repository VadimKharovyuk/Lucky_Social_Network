package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AuthenticationService;
import com.example.lucky_social_network.service.UserActivityService;
import com.example.lucky_social_network.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
    private final UserActivityService userActivityService;
    private final AuthenticationService authenticationService;

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
            userActivityService.trackUserLogin(user);
            authenticationService.handleLogin(user);


            return "redirect:/posts";
        } catch (Exception e) {
            log.error("Login error for user {}: {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка входа в систему");
            return "redirect:/login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        // Получаем текущего пользователя до очистки контекста
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User currentUser = (User) auth.getPrincipal();

            try {
                // Обработка статистики перед выходом
                authenticationService.handleLogout(currentUser);

                // Выполняем выход
                new SecurityContextLogoutHandler().logout(request, response, auth);

                log.info("User {} logged out successfully", currentUser.getUsername());
            } catch (Exception e) {
                log.error("Error during logout for user {}: {}",
                        currentUser.getUsername(), e.getMessage());
            }
        }

        // Очищаем контекст безопасности
        SecurityContextHolder.clearContext();

        redirectAttributes.addFlashAttribute("message", "Вы успешно вышли из системы");
        return "redirect:/login?logout";
    }
}