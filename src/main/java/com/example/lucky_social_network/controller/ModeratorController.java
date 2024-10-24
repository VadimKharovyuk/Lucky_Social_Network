package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.ChatService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
public class ModeratorController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/chats")
    public String viewChats(@RequestParam(required = false) String search, Model model) {
        List<User> users;
        if (search != null && !search.isEmpty()) {
            users = chatService.searchUsersWithChats(search);
        } else {
            users = chatService.getAllUsersWithChats();
        }
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "moderator/chats";
    }

    @GetMapping("/chat-history")
    public String viewChatHistory(@RequestParam Long user1Id, @RequestParam Long user2Id, Model model) {
        User user1 = chatService.getUserById(user1Id);
        User user2 = chatService.getUserById(user2Id);
        List<Message> chatHistory = chatService.getChatHistory(user1Id, user2Id);

        model.addAttribute("user1", user1);
        model.addAttribute("user2", user2);
        model.addAttribute("chatHistory", chatHistory);

        return "moderator/chatHistory";
    }


    @GetMapping("/chat")
    public String viewChat(@RequestParam Long userId, Model model) {
        User user = chatService.getUserById(userId);
        List<User> chatPartners = chatService.getUserChats(userId);
        model.addAttribute("user", user);
        model.addAttribute("chatPartners", chatPartners);
        return "moderator/chatDetail";
    }


    @PostMapping("/user/{userId}/warn")
    public String warnUser(@PathVariable Long userId, @RequestParam Long otherUserId) {
        chatService.warnUser(userId);
        log.info("User {} warned by moderator", userId);
        return "redirect:/moderator/chat?user1Id=" + userId + "&user2Id=" + otherUserId;
    }
}