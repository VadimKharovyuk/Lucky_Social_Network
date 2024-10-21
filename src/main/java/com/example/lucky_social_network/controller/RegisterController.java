package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private  final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        // Проверяем, существует ли пользователь с таким именем
        if (userService.isUsernameExists(user.getUsername())) {
            model.addAttribute("error", "Пользователь с именем '" + user.getUsername() + "' уже существует. Пожалуйста, выберите другое имя.");
            return "register";
        }

        try {
            user.setCreatedAt(LocalDate.now());
            user.setEmailVerified(false);
            user.setIsPrivate(false);
            userService.registerNewUser(user);

            redirectAttributes.addFlashAttribute("success", "Регистрация успешно завершена!");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }
}
