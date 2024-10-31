package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.CreateWorkExperienceDTO;
import com.example.lucky_social_network.dto.WorkExperienceDTO;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import com.example.lucky_social_network.service.WorkExperienceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users/{userId}/work")
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;
    private final UserService userService;

    @GetMapping
    public String showWorkExperienceList(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();
        User targetUser = userService.getUserById(userId);

        model.addAttribute("workExperienceList", workExperienceService.getAllWorkExperience(userId));
        model.addAttribute("user", targetUser);
        model.addAttribute("isOwner", currentUser.getId().equals(userId));

        return "work-experience/list";
    }

    @GetMapping("/add")
    public String showAddForm(@PathVariable Long userId, Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        model.addAttribute("workExperienceDTO", new CreateWorkExperienceDTO());
        model.addAttribute("user", currentUser);
        model.addAttribute("isEdit", false);
        return "work-experience/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long userId,
                               @PathVariable Long id,
                               Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        WorkExperienceDTO workExperience = workExperienceService.getWorkExperienceById(userId, id);
        model.addAttribute("workExperienceDTO", workExperience);
        model.addAttribute("user", currentUser);
        model.addAttribute("isEdit", true); // Добавляем этот атрибут
        return "work-experience/form";
    }

    @PostMapping("/add")
    public String addWorkExperience(@PathVariable Long userId,
                                    @Valid @ModelAttribute("workExperienceDTO") CreateWorkExperienceDTO dto,
                                    BindingResult result,
                                    Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "work-experience/form";
        }

        workExperienceService.addWorkExperience(userId, dto);
        return "redirect:/users/" + userId + "/work";
    }


    @PostMapping("/{id}/edit")
    public String updateWorkExperience(@PathVariable Long userId,
                                       @PathVariable Long id,
                                       @Valid @ModelAttribute("workExperienceDTO") CreateWorkExperienceDTO dto,
                                       BindingResult result,
                                       Model model) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "work-experience/form";
        }

        workExperienceService.updateWorkExperience(userId, id, dto);
        return "redirect:/users/" + userId + "/work";
    }

    @PostMapping("/{id}/delete")
    public String deleteWorkExperience(@PathVariable Long userId,
                                       @PathVariable Long id) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            return "error/403";
        }

        workExperienceService.deleteWorkExperience(userId, id);
        return "redirect:/users/" + userId + "/work";
    }
}
