//package com.example.lucky_social_network.controller;
//
//import com.example.lucky_social_network.model.Message;
//import com.example.lucky_social_network.service.ChatService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/chat")
//@RequiredArgsConstructor
//public class ChatController {
//
//    private final ChatService chatService;
//
//    @PostMapping("/send")
//    public ResponseEntity<Message> sendMessage(@RequestParam Long senderId,
//                                               @RequestParam Long recipientId,
//                                               @RequestParam String content) {
//        Message message = chatService.sendMessage(senderId, recipientId, content);
//        return ResponseEntity.ok(message);
//    }
//
//    @GetMapping("/history")
//    public ResponseEntity<List<Message>> getChatHistory(@RequestParam Long senderId,
//                                                        @RequestParam Long recipientId) {
//        List<Message> messages = chatService.getChatHistory(senderId, recipientId);
//        return ResponseEntity.ok(messages);
//    }
//}


package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import com.example.lucky_social_network.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//@Controller // Изменяем RestController на Controller для работы с HTML
//@RequestMapping("/chat")
//@RequiredArgsConstructor
//public class ChatController {
//
//    private final ChatService chatService;
//    private final UserRepository  userRepository;
//    // Метод для отображения формы отправки сообщения
//    @GetMapping("/send")
//    public String showSendMessageForm(Model model, @SessionAttribute("currentUser") User currentUser) {
//        model.addAttribute("currentUser", currentUser); // Добавляем текущего пользователя в модель
//        model.addAttribute("message", new Message());
//        return "sendMessage"; // Имя HTML-файла, который будет отображен
//    }
//
//    // Метод для отправки сообщения
//    @PostMapping("/send")
//    public String sendMessage(@RequestParam(required = false) Long senderId,
//                              @RequestParam Long recipientId,
//                              @RequestParam String content) {
//        // Если senderId не передан, используем текущего пользователя
//        if (senderId == null) {
//            // Получить ID текущего пользователя из сессии или другого источника
//            Optional<User >user = userRepository.findById(recipientId); // Ваша логика получения ID текущего пользователя
//        }
//        // Сохранение сообщения в базе данных
//        chatService.sendMessage(senderId, recipientId, content);
//        return "redirect:/chat/history"; // Перенаправление на историю сообщений
//    }
//
//
//
//    // Метод для получения истории сообщений
//    @GetMapping("/history")
//    public String getChatHistory(Model model, @RequestParam Long senderId, @RequestParam Long recipientId) {
//        List<Message> messages = chatService.getChatHistory(senderId, recipientId);
//        model.addAttribute("messages", messages);
//        return "chatHistory"; // Возвращает имя вашего HTML-шаблона для истории сообщений
//    }
//}



import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.ChatService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
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