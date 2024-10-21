package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingsController {
    private final UserService userService;

    @GetMapping
    public String settings(Model model) {
        User user = userService.getCurrentUser();


        model.addAttribute("user", user);

        return "settings/Template";
    }
}
