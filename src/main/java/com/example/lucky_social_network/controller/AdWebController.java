package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.AdCreateRequest;
import com.example.lucky_social_network.dto.AdDTO;
import com.example.lucky_social_network.dto.AdUpdateRequest;
import com.example.lucky_social_network.model.Ad;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AdService;
import com.example.lucky_social_network.service.GroupService;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdWebController {
    private final AdService adService;
    private final GroupService groupService;
    private final UserService userService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User currentUser = userService.getCurrentUser();
        log.info("Создание формы рекламы для пользователя ID: {}", currentUser.getId());

        // Создаем пустой запрос с предустановленными значениями
        AdCreateRequest emptyRequest = AdCreateRequest.createEmpty();
        log.info("Создан пустой запрос с {} расписаниями", emptyRequest.schedules().size());

        // Получаем группы, где пользователь имеет права администратора
        List<Group> userGroups = groupService.getGroupsWithAdminAccess(currentUser.getId());
        log.info("Получено {} групп с правами администратора", userGroups.size());

        // Добавляем все необходимые атрибуты в модель
        model.addAttribute("adStatuses", Ad.AdStatus.values());
        model.addAttribute("adCreateRequest", emptyRequest);
        model.addAttribute("userGroups", userGroups); // Используется для выбора owningGroupId и targetGroupIds
        model.addAttribute("currentUser", currentUser);

        return "ads/create";
    }

    @PostMapping("/create")
    public String createAd(
            @Valid @ModelAttribute("adCreateRequest") AdCreateRequest request,
            BindingResult bindingResult,
            Model model) {
        User currentUser = userService.getCurrentUser();

        if (bindingResult.hasErrors()) {
            log.error("Ошибки валидации формы: {}", bindingResult.getAllErrors());
            model.addAttribute("userGroups",
                    groupService.getGroupsWithAdminAccess(currentUser.getId()));
            return "ads/create";
        }

        try {
            adService.createAd(request, currentUser);
            return "redirect:/ads?success=created";
        } catch (IllegalArgumentException e) {
            // Ошибки валидации
            log.error("Ошибка валидации: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userGroups",
                    groupService.getGroupsWithAdminAccess(currentUser.getId()));
            return "ads/create";
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userGroups",
                    groupService.getGroupsWithAdminAccess(currentUser.getId()));
            return "ads/create";
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании рекламы: ", e);
            model.addAttribute("error", "Произошла ошибка при создании рекламы");
            model.addAttribute("userGroups",
                    groupService.getGroupsWithAdminAccess(currentUser.getId()));
            return "ads/create";
        }
    }

    @GetMapping
    public String listAds(Model model) {
        User currentUser = userService.getCurrentUser();

        List<AdDTO> activeAds = adService.getActiveAds();
        List<AdDTO> userAds = adService.getAdsByOwner(currentUser.getId());

        model.addAttribute("activeAds", activeAds);
        model.addAttribute("userAds", userAds);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("adStatuses", Ad.AdStatus.values());
        return "ads/list";
    }


    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser) {
        AdDTO ad = adService.getAd(id);
        model.addAttribute("ad", ad);
        model.addAttribute("userGroups", groupService.getGroupsByUser(currentUser.getId()));
        return "ads/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAd(
            @PathVariable Long id,
            @Valid @ModelAttribute("adUpdateRequest") AdUpdateRequest request,
            BindingResult bindingResult) {
        User currentUser = userService.getCurrentUser();

        if (bindingResult.hasErrors()) {
            return "ads/edit";
        }

        try {
            adService.updateAd(id, request, currentUser);
            return "redirect:/ads?success=updated";
        } catch (AccessDeniedException e) {
            bindingResult.reject("error.access", e.getMessage());
            return "ads/edit";
        }
    }

    @PostMapping("/{id}/activate")
    public String activateAd(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adService.activateAd(id);
            redirectAttributes.addFlashAttribute("success", "Реклама успешно активирована");
        } catch (Exception e) {
            log.error("Ошибка при активации рекламы: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ads";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateAd(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adService.deactivateAd(id);
            redirectAttributes.addFlashAttribute("success", "Реклама деактивирована");
        } catch (Exception e) {
            log.error("Ошибка при деактивации рекламы: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ads";
    }
}