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
        User sender = userService.getUserById(senderId);
        User recipient = userService.getUserById(recipientId);
        List<Message> chatHistory = chatService.getChatHistory(senderId, recipientId);

        model.addAttribute("sender", sender);
        model.addAttribute("recipient", recipient);
        model.addAttribute("chatHistory", chatHistory);
        model.addAttribute("newMessage", new Message());

        return "chat";
    }

    @PostMapping("/send")
    public String sendMessage(@ModelAttribute("newMessage") Message newMessage,
                              @RequestParam Long senderId,
                              @RequestParam Long recipientId) {
        chatService.sendMessage(senderId, recipientId, newMessage.getContent());
        return "redirect:/chat/" + senderId + "/" + recipientId;
    }


    @GetMapping("/start/{recipientId}")
    public String startNewChat(@PathVariable Long recipientId) {
        Long currentUserId = getCurrentUserId();
        return "redirect:/chat/" + currentUserId + "/" + recipientId;
    }



    @GetMapping("/list")
    public String getUserChats(Model model) {

        Long currentUserId = getCurrentUserId();  // Получаем ID текущего пользователя
        model.addAttribute("currentUserId", getCurrentUserId());
        List<User> chatUsers = chatService.getUserChats(currentUserId);  // Получаем список чатов
        model.addAttribute("chatUsers", chatUsers);
        return "chatList";  // Возвращаем имя шаблона
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