package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.AdDTO;
import com.example.lucky_social_network.model.Ad;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AdService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ads-management")
@RequiredArgsConstructor
@Slf4j
public class AdManagementController {
    private final AdService adService;
    private final UserService userService;

    @GetMapping
    public String showAdManagement(Model model) {
        User currentUser = userService.getCurrentUser();
        // Получаем все рекламы пользователя
        List<AdDTO> userAds = adService.getAllAdsByOwner(currentUser.getId());

        log.info("Found {} ads for user {}", userAds.size(), currentUser.getId());
        userAds.forEach(ad ->
                log.info("Ad ID: {}, Title: {}, Status: {}", ad.id(), ad.title(), ad.status())
        );

        model.addAttribute("ads", userAds);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("adStatuses", Ad.AdStatus.values()); // Добавляем все возможные статусы

        return "ads/management";
    }

    @PostMapping("/{id}/activate")
    public String activateAd(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adService.activateAd(id);
            log.info("Ad {} successfully activated", id);
            redirectAttributes.addFlashAttribute("success", "Реклама успешно активирована");
        } catch (Exception e) {
            log.error("Error activating ad {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при активации рекламы: " + e.getMessage());
        }
        return "redirect:/ads-management";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateAd(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adService.deactivateAd(id);
            log.info("Ad {} successfully deactivated", id);
            redirectAttributes.addFlashAttribute("success", "Реклама деактивирована");
        } catch (Exception e) {
            log.error("Error deactivating ad {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при деактивации рекламы: " + e.getMessage());
        }
        return "redirect:/ads-management";
    }
}
