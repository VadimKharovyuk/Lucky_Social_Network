package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.ChatService;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;


    @GetMapping("/{senderId}/{recipientId}")
    public String getChatPage(@PathVariable Long senderId,
                              @PathVariable Long recipientId,
                              Model model) {
        User currentUser = userService.getUserById(senderId);
        if (currentUser == null) {
            return "redirect:/login";
        }

        User recipient = userService.getUserById(recipientId);
        if (recipient == null) {
            return "redirect:/messages";
        }

        // Проверяем, что пользователь не пытается отправить сообщение самому себе
        if (currentUser.getId().equals(recipientId)) {
            return "redirect:/messages";
        }

        List<Message> chatHistory = chatService.getChatHistory(senderId, recipientId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("recipient", recipient);
        model.addAttribute("chatHistory", chatHistory);

        return "chat";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("content") String content,
                              @RequestParam("senderId") Long senderId,
                              @RequestParam("recipientId") Long recipientId,
                              RedirectAttributes redirectAttributes) {
        try {
            chatService.sendMessage(senderId, recipientId, content);
            return "redirect:/chat/" + senderId + "/" + recipientId + "#bottom";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось отправить сообщение");
            return "redirect:/chat/" + senderId + "/" + recipientId;
        }
    }


    @GetMapping("/list")
    public String getUserChats(Model model) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = getCurrentUserId();  // Получаем ID текущего пользователя
        model.addAttribute("currentUserId", getCurrentUserId());
        List<User> chatUsers = chatService.getUserChats(currentUserId);  // Получаем список чатов
        model.addAttribute("chatUsers", chatUsers);
        model.addAttribute("currentUser", currentUser);
        return "chatList";  // Возвращаем имя шаблона
    }

    @GetMapping("/start/{recipientId}")
    public String startNewChat(@PathVariable Long recipientId) {
        Long currentUserId = getCurrentUserId();
        return "redirect:/chat/" + currentUserId + "/" + recipientId;
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