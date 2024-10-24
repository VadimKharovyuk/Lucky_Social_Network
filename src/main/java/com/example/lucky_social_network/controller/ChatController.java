package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.ChatService;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // * Удаление всей истории чата
//     */
    @PostMapping("/delete-history")
    public String deleteMessageHistory(@RequestParam("userId") Long userId,
                                       @RequestParam("partnerId") Long partnerId,
                                       @RequestParam(value = "forCurrentUserOnly", defaultValue = "true") boolean forCurrentUserOnly,
                                       RedirectAttributes redirectAttributes) {
        try {
            chatService.deleteMessagesForUser(userId, partnerId, forCurrentUserOnly);

            String successMessage = forCurrentUserOnly ?
                    "История чата удалена для вас" :
                    "История чата удалена для всех участников";
            redirectAttributes.addFlashAttribute("success", successMessage);

            log.info("Chat history deleted. userId={}, partnerId={}, forCurrentUserOnly={}",
                    userId, partnerId, forCurrentUserOnly);

            return "redirect:/chat/" + userId + "/" + partnerId;

        } catch (Exception e) {
            log.error("Error deleting chat history: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить историю чата");
            return "redirect:/chat/" + userId + "/" + partnerId;
        }
    }

    /**
     * Удаление одного сообщения
     */
    @PostMapping("/message/{messageId}/delete")
    public String deleteMessage(@PathVariable Long messageId,
                                @RequestParam("userId") Long userId,
                                @RequestParam("recipientId") Long recipientId, // Добавляем recipientId
                                @RequestParam(value = "forEveryone", defaultValue = "false") boolean forEveryone,
                                RedirectAttributes redirectAttributes) {
        try {
            chatService.deleteMessage(messageId, userId, forEveryone);

            String successMessage = forEveryone ?
                    "Сообщение удалено для всех" :
                    "Сообщение удалено для вас";
            redirectAttributes.addFlashAttribute("success", successMessage);

            log.info("Message deleted. messageId={}, userId={}, recipientId={}, forEveryone={}",
                    messageId, userId, recipientId, forEveryone);

        } catch (Exception e) {
            log.error("Error deleting message: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить сообщение");
        }

        // Перенаправляем обратно в чат с обоими ID
        return "redirect:/chat/" + userId + "/" + recipientId;
    }

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
        } catch (RuntimeException e) {
            log.error("Error sending message: {}", e.getMessage());
            String errorMessage = "Не удалось отправить сообщение: " +
                    (e.getMessage() != null ? e.getMessage() : "неизвестная ошибка");
            redirectAttributes.addFlashAttribute("error", errorMessage);
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