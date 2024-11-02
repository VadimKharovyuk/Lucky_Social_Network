package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.SocialLinkCreateDTO;
import com.example.lucky_social_network.dto.SocialLinkResponseDTO;
import com.example.lucky_social_network.dto.SocialLinkUpdateDTO;
import com.example.lucky_social_network.model.SocialLink;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.SocialLinkService;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/social-links")
@RequiredArgsConstructor
public class SocialLinkController {
    private final SocialLinkService socialLinkService;
    private final UserService userService;

    @GetMapping("/manage")
    public String manageSocialLinks(Model model) {
        User currentUser = userService.getCurrentUser();
        List<SocialLinkResponseDTO> userLinks = socialLinkService.getAllByUserId(currentUser.getId());

        model.addAttribute("userLinks", userLinks);
        model.addAttribute("user", currentUser);
        model.addAttribute("createDto", new SocialLinkCreateDTO());
        model.addAttribute("platforms", SocialLink.SocialPlatform.values());
        model.addAttribute("link", new SocialLink());

        return "social-links/manage";
    }

    @PostMapping("/create")
    public String createSocialLink(@Valid @ModelAttribute SocialLinkCreateDTO dto,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Attempting to create social link: {}", dto); // Добавьте это

        if (result.hasErrors()) {
            log.error("Validation errors: {}", result.getAllErrors()); // И это
            redirectAttributes.addFlashAttribute("error", "Проверьте правильность введенных данных");
            return "redirect:/social-links/manage";
        }

        try {
            User currentUser = userService.getCurrentUser();
            dto.setUserId(currentUser.getId());
            log.debug("Setting userId to: {}", currentUser.getId()); // И это
            socialLinkService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Социальная ссылка успешно добавлена");
        } catch (Exception e) {
            log.error("Error creating social link", e); // И это
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении ссылки: " + e.getMessage());
        }

        return "redirect:/social-links/manage";
    }

    // Обновление существующей ссылки
    @PostMapping("/update/{id}")
    public String updateSocialLink(@PathVariable Long id,
                                   @Valid @ModelAttribute SocialLinkUpdateDTO dto,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Проверьте правильность введенных данных");
            return "redirect:/social-links/manage";
        }

        try {
            dto.setId(id);
            socialLinkService.update(dto);
            redirectAttributes.addFlashAttribute("success", "Социальная ссылка успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении ссылки: " + e.getMessage());
        }

        return "redirect:/social-links/manage";
    }

    // Удаление ссылки
    @PostMapping("/delete/{id}")
    public String deleteSocialLink(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            socialLinkService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Социальная ссылка успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении ссылки: " + e.getMessage());
        }

        return "redirect:/social-links/manage";
    }

}