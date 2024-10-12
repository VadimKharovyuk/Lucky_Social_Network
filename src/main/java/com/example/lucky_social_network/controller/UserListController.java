package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.exception.FriendshipNotFoundException;
import com.example.lucky_social_network.exception.UserNotFoundException;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.ChatService;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserListController {
    private final UserService userService;



    @PostMapping("/addFriend")
    public String addFriend(@RequestParam Long userId, @RequestParam Long friendId, RedirectAttributes redirectAttributes) {
        try {
            userService.addFriend(userId, friendId);
            redirectAttributes.addFlashAttribute("message", "Friend added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add friend: " + e.getMessage());
        }
        return "redirect:/users/list";
    }



    @GetMapping("/list")
    public String getUserList(Model model) {
        Long currentUserId = getCurrentUserId();

        // Получаем текущего пользователя
        User currentUser = userService.getUserById(currentUserId);
        model.addAttribute("currentUser", currentUser);

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUserId", getCurrentUserId());

        return "user-list";
    }

//поиск друзей по id
// Поиск друзей текущего пользователя
@GetMapping("/friends")
public String getFriends(Model model) {
    Long currentUserId = getCurrentUserId(); // Получаем ID текущего пользователя
    Set<User> friends = userService.getFriendsByUser(currentUserId); // Используем ID текущего пользователя
    model.addAttribute("friends", friends); // Добавляем список друзей в модель
    model.addAttribute("currentUserId", currentUserId); // Добавляем ID текущего пользователя в модель
    return "friendsList"; // Возвращаем имя шаблона
}

    @PostMapping("/delete/{friendId}")
    public String removeFriend(@PathVariable Long friendId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Long userId = ((CustomUserDetails) userDetails).getId();
            userService.removeFriend(userId, friendId);
            redirectAttributes.addFlashAttribute("message", "Друг успешно удален");
        } catch (UserNotFoundException | FriendshipNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при удалении друга");
        }
        return "redirect:/users/friends";
    }








    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated or CustomUserDetails not found");
    }


}