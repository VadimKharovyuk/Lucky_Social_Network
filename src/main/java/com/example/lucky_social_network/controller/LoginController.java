package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
@RequiredArgsConstructor
@Controller

public class LoginController {

    private  final  AuthenticationManager authenticationManager;
    private  final  UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findByUsername(username);
            userService.updateLastLogin(user);

            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
            return "login";
        }
    }

//    @PostMapping("/login")
//    public String processLogin(@RequestParam String username,
//                               @RequestParam String password,
//                               Model model) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(username, password)
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            User user = userService.findByUsername(username);
//            user.setLastLogin(LocalDateTime.now());
//            userService.updateUser(user);
//
//            return "redirect:/home";
//        } catch (Exception e) {
//            model.addAttribute("error", "Неверное имя пользователя или пароль");
//            return "login";
//        }
//    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }
}