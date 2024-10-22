package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.SupportTicket;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.SupportTicketService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingsController {
    private final UserService userService;
    private final SupportTicketService supportTicketService;

    @GetMapping
    public String settings(Model model) {
        User user = userService.getCurrentUser();


        model.addAttribute("ticket", new SupportTicket());
        model.addAttribute("user", user);

        return "settings/Template";
    }

    @PostMapping
    public String submitTicket(@AuthenticationPrincipal UserDetails userDetails,
                               @ModelAttribute("ticket") SupportTicket ticket,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.getCurrentUser());
            return "settings/Template";
        }

        try {
            // Получаем текущего пользователя из базы данных
            User user = userService.getUserByUsername(userDetails.getUsername());

            // Создаем тикет с существующим пользователем
            SupportTicket createdTicket = supportTicketService.createTicket(
                    user,
                    ticket.getSubject(),
                    ticket.getMessage()
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Тикет #" + createdTicket.getId() + " успешно создан");

            return "redirect:/settings";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании тикета: " + e.getMessage());
            return "redirect:/settings";
        }
    }
}
