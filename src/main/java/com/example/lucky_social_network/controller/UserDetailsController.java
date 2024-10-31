package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.exception.EmailAlreadyExistsException;
import com.example.lucky_social_network.model.Notification;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/users/{userId}/details")
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class UserDetailsController {

    private final UserService userService;


    @GetMapping
    public String showUserDetails(@PathVariable Long userId, Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            User targetUser = userService.getUserById(userId);

            // Получаем URL аватара
            String avatar = userService.getUserAvatarUrl(targetUser);

            model.addAttribute("user", targetUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isOwner", currentUser.getId().equals(userId));
            model.addAttribute("avatar", avatar);

            long notificationCount = 0;
            List<Notification> recentNotifications = new ArrayList<>();

            model.addAttribute("notificationCount", notificationCount);
            model.addAttribute("recentNotifications", recentNotifications);

            return "user/details";

        } catch (Exception e) {
            log.error("Error showing user details for userId: {}", userId, e);
            return "error/500";
        }

    }

    @GetMapping("/edit")
    public String showEditDetailsForm(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        // Добавьте другие необходимые атрибуты для формы

        return "user/edit-details";
    }

    @PostMapping("/edit")
    public String updateUserDetails(@PathVariable Long userId,
                                    @Valid @ModelAttribute User user,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        try {
            if (result.hasErrors()) {
                return "user/edit-details";
            }

            userService.updateUserDetails(user);
            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
            return "redirect:/users/" + userId + "/details";

        } catch (EmailAlreadyExistsException e) {
            result.rejectValue("email", "error.email", "Этот email уже используется");
            return "user/edit-details";
        } catch (Exception e) {
            log.error("Error updating user details: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при обновлении профиля");
            return "redirect:/users/" + userId + "/details";
        }
    }
}

