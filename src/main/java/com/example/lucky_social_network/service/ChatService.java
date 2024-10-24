package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.MessageRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
//    private final RabbitMQService rabbitMQService;

    public Message sendMessage(Long senderId, Long recipientId, String content) {
        // Проверяем существование пользователей
        User sender = userService.getUserById(senderId);
        User recipient = userService.getUserById(recipientId);

        if (sender == null || recipient == null) {
            throw new RuntimeException("Sender or recipient not found");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        // Логируем сообщение перед сохранением
        log.info("Saving message: from {} to {}: {}", senderId, recipientId, content);

        Message savedMessage = messageRepository.save(message);

        // Проверяем сохранение
        log.info("Message saved with id: {}", savedMessage.getId());

        return savedMessage;
    }

    public List<Message> getChatHistory(Long senderId, Long recipientId) {
        List<Message> messages = messageRepository.findChatHistory(senderId, recipientId);
        return messages;
    }
//    @Transactional
//    public Message sendMessage(Long senderId, Long recipientId, String content) {
//        User sender = getUserById(senderId);
//        User recipient = getUserById(recipientId);
//
//        Message message = new Message();
//        message.setSender(sender);
//        message.setRecipient(recipient);
//        message.setContent(content);
//        message.setTimestamp(LocalDateTime.now());
//
//        return messageRepository.save(message);
//    }
//
//    public List<Message> getChatHistory(Long senderId, Long recipientId) {
//        return messageRepository.findChatHistory(senderId, recipientId);
//    }

    public List<User> getUserChats(Long userId) {
        User currentUser = getUserById(userId);
        List<Message> userMessages = messageRepository.findBySenderOrRecipient(currentUser, currentUser);

        return userMessages.stream()
                .map(message -> message.getSender().getId().equals(userId) ? message.getRecipient() : message.getSender())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<User> getAllUsersWithChats() {
        return userRepository.findAll().stream()
                .filter(user -> !getUserChats(user.getId()).isEmpty())
                .collect(Collectors.toList());
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
        log.info("Message with id {} has been deleted", messageId);
    }

    @Transactional
    public void warnUser(Long userId) {
        User user = getUserById(userId);
        // Здесь можно реализовать логику предупреждения пользователя
        // Например, увеличить счетчик предупреждений или отправить уведомление
        log.info("User {} has been warned", user.getUsername());
    }

    public List<User> searchUsersWithChats(String searchTerm) {
        return userRepository.findByLastNameOrFirstNameOrUsernameContainingIgnoreCase(searchTerm);
    }
}

