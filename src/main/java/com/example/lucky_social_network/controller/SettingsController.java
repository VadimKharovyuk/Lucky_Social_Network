package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PasswordChangeDTO;
import com.example.lucky_social_network.dto.UserDisplayDTO;
import com.example.lucky_social_network.model.SupportTicket;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.SupportTicketService;
import com.example.lucky_social_network.service.UserService;
import com.example.lucky_social_network.service.picService.ImgurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingsController {
    private final UserService userService;
    private final SupportTicketService supportTicketService;
    private final ImgurService imgurService;

    @GetMapping
    public String settings(Model model) {
        try {
            User user = userService.getCurrentUser();
            String avatarUrl = userService.getUserAvatarUrl(user);

            // Создаем DTO для отображения
            UserDisplayDTO userDisplay = UserDisplayDTO.fromUser(user);

            // Добавляем все необходимые атрибуты в модель
            model.addAttribute("avatarUrl", avatarUrl);
            model.addAttribute("ticket", new SupportTicket());
            model.addAttribute("user", user);
            model.addAttribute("userDisplay", userDisplay);
            model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());

            return "settings/Template";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при загрузке настроек: " + e.getMessage());
            return "redirect:/login";
        }
    }


    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeDTO passwordChangeDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        try {
            // Проверяем совпадение паролей
            if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.passwordChangeDTO",
                        "Пароли не совпадают");
            }

            if (bindingResult.hasErrors()) {
                // Добавляем все необходимые атрибуты в модель при ошибке
                User currentUser = userService.getCurrentUser();
                model.addAttribute("user", currentUser);
                model.addAttribute("avatarUrl", userService.getUserAvatarUrl(currentUser));
                model.addAttribute("ticket", new SupportTicket());
                model.addAttribute("userDisplay", UserDisplayDTO.fromUser(currentUser));
                model.addAttribute("errorMessage", "Проверьте правильность введенных данных");

                return "settings/Template";
            }

            User currentUser = userService.getCurrentUser();
            userService.changePassword(
                    currentUser.getId(),
                    passwordChangeDTO.getCurrentPassword(),
                    passwordChangeDTO.getNewPassword()
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Пароль успешно изменен");
            return "redirect:/settings";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage());
        } catch (Exception e) {
            log.error("Error changing password", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при смене пароля");
        }

        return "redirect:/settings";
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
