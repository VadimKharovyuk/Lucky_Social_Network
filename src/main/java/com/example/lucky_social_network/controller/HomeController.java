package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;


    @GetMapping
    public String home(Model model) {
        // Получаем ID текущего пользователя
        Long currentUserId = getCurrentUserId();

        // Получаем профиль текущего пользователя
        User currentUser = userService.getUserProfileById(currentUserId);

        // Добавляем пользователя в модель
        model.addAttribute("currentUser", currentUser);
        return "home";
    }
    @GetMapping("/home")
    public String homePage( ) {
        return "homePage";
    }




    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated or CustomUserDetails not found");
    }
}
