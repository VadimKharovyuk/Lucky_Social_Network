package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.RelationshipStatusConstants;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    //профиль по id
    @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model) {
        User user = userService.getUserProfileById(userId);

        // Получаем ID текущего пользователя
        Long currentUserId = getCurrentUserId();

        // Получаем текущего пользователя
        User currentUser = userService.getUserById(currentUserId);
        model.addAttribute("currentUser", currentUser);

        // Проверяем, являются ли пользователи друзьями
        boolean areFriends = userService.areFriends(currentUserId, userId);
        model.addAttribute("areFriends", areFriends);
        model.addAttribute("user", user);
        return "user-profile";
    }
//профиль пользователя
    @GetMapping
    public String getProfile(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("relationshipStatuses", RelationshipStatusConstants.getAllStatuses());
        model.addAttribute("user", user);
        return "profile";
    }

//обновиить профиль
//    @PostMapping("/update")
//    public String updateProfile(@ModelAttribute User updatedUser, @RequestParam("avatarFile") MultipartFile avatarFile) throws IOException {
//        User existingUser = userService.getUserById(updatedUser.getId());
//
//        // Обновляем только измененные поля
//        existingUser.setEmail(updatedUser.getEmail());
//        existingUser.setPhone(updatedUser.getPhone());
//        existingUser.setBio(updatedUser.getBio());
//        existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
//        existingUser.setLocation(updatedUser.getLocation());
//
//
//        // Если пользователь загрузил новый аватар, обновляем его
//        if (!avatarFile.isEmpty()) {
//            existingUser.setAvatar(avatarFile.getBytes());
//        }
//        userService.updateUser(existingUser);
//        return "redirect:/profile";
//    }
@PostMapping("/update")
public String updateProfile(@ModelAttribute User updatedUser,
                            @RequestParam("avatarFile") MultipartFile avatarFile,
                            @RequestParam(required = false) String relationshipStatus,
                            @RequestParam(required = false) Long partnerId) throws IOException {
    // Если пользователь загрузил новый аватар, обновляем его
    if (!avatarFile.isEmpty()) {
        updatedUser.setAvatar(avatarFile.getBytes());
    }

    userService.updateUser(updatedUser, relationshipStatus, partnerId);
    return "redirect:/profile";
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