package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String getProfile(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "profile";
    }


    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser, @RequestParam("avatarFile") MultipartFile avatarFile) throws IOException {
        User existingUser = userService.getUserById(updatedUser.getId());

        // Обновляем только измененные поля
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setBio(updatedUser.getBio());
        existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        existingUser.setLocation(updatedUser.getLocation());

        // Если пользователь загрузил новый аватар, обновляем его
        if (!avatarFile.isEmpty()) {
            existingUser.setAvatar(avatarFile.getBytes());
        }
        userService.updateUser(existingUser);
        return "redirect:/profile";
    }
}