package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.UserInterestsDTO;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserInterestsService;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
@RequestMapping("/users/{userId}/interests")
public class UserInterestsController {

    private final UserInterestsService userInterestsService;
    private final UserService userService;

    @GetMapping("/add")
    public String showAddForm(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }


        // Создаем пустой DTO для формы
        UserInterestsDTO interestsDTO = new UserInterestsDTO();
        interestsDTO.setUserId(userId);

        model.addAttribute("interestsDTO", interestsDTO);
        model.addAttribute("isNew", true);
        model.addAttribute("user", currentUser);
        return "interests/edit";
    }

    // Обработка создания
    @PostMapping("/add")
    public String createInterests(@PathVariable Long userId,
                                  @Valid @ModelAttribute UserInterestsDTO dto,
                                  BindingResult result) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        if (result.hasErrors()) {
            return "interests/edit";
        }

        userInterestsService.createUserInterests(userId, dto);
        return "redirect:/users/" + userId + "/details";
    }


    @GetMapping("/edit")
    public String showEditForm(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        UserInterestsDTO interests = userInterestsService.getUserInterests(userId);
        model.addAttribute("interestsDTO", interests);
        model.addAttribute("user", currentUser);
        return "interests/edit";
    }

    @PostMapping("/edit")
    public String updateInterests(@PathVariable Long userId,
                                  @Valid @ModelAttribute UserInterestsDTO dto,
                                  BindingResult result) {
        if (result.hasErrors()) {
            return "interests/edit";
        }

        userInterestsService.updateUserInterests(userId, dto);
        return "redirect:/users/" + userId + "/details";
    }
}


