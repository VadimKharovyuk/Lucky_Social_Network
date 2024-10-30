package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.CreateEducationDTO;
import com.example.lucky_social_network.dto.EducationDTO;
import com.example.lucky_social_network.model.Education;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.EducationService;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users/{userId}/education")
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class EducationController {
    private final EducationService educationService;
    private final UserService userService;


    @GetMapping
    public String showEducationList(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();
        User targetUser = userService.getUserById(userId);

        model.addAttribute("educationList", educationService.getAllEducation(userId));
        model.addAttribute("user", targetUser);
        model.addAttribute("isOwner", currentUser.getId().equals(userId));
        model.addAttribute("educationTypes", Education.EducationType.values());

        return "education/list";
    }

    @GetMapping("/add")
    public String showAddForm(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        model.addAttribute("educationDTO", new CreateEducationDTO());
        model.addAttribute("educationTypes", Education.EducationType.values());
        model.addAttribute("user", currentUser);
        return "education/form";
    }

    @PostMapping("/add")
    public String addEducation(@PathVariable Long userId,
                               @Valid @ModelAttribute("educationDTO") CreateEducationDTO dto,
                               BindingResult result,
                               Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        if (result.hasErrors()) {
            model.addAttribute("educationTypes", Education.EducationType.values());
            model.addAttribute("user", currentUser);
            return "education/form";
        }

        educationService.addEducation(userId, dto);
        return "redirect:/users/" + userId + "/education";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long userId,
                               @PathVariable Long id,
                               Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        EducationDTO education = educationService.getEducationById(userId, id);
        model.addAttribute("educationDTO", education);
        model.addAttribute("educationTypes", Education.EducationType.values());
        model.addAttribute("user", currentUser);
        model.addAttribute("isEdit", true);
        return "education/form";
    }


    @PostMapping("/{id}/delete")
    public String deleteEducation(@PathVariable Long userId,
                                  @PathVariable Long id) {


        User currentUser = userService.getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }
        educationService.deleteEducation(userId, id);
        return "redirect:/users/" + userId + "/education";
    }
}
