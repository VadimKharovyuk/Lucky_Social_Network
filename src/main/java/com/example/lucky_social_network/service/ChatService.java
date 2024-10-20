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

    @Transactional
    public Message sendMessage(Long senderId, Long recipientId, String content) {
        User sender = getUserById(senderId);
        User recipient = getUserById(recipientId);

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    public List<Message> getChatHistory(Long senderId, Long recipientId) {
        return messageRepository.findChatHistory(senderId, recipientId);
    }

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


//package com.example.lucky_social_network.service;
//
//import com.example.lucky_social_network.model.Message;
//import com.example.lucky_social_network.model.User;
//import com.example.lucky_social_network.repository.MessageRepository;
//import com.example.lucky_social_network.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ChatService {
//
//    private final MessageRepository messageRepository;
//    private final UserRepository userRepository;
//
//
//    @Transactional
//    public Message sendMessage(Long senderId, Long recipientId, String content) {
//        User sender = userRepository.findById(senderId)
//                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
//        User recipient = userRepository.findById(recipientId)
//                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
//
//        Message message = new Message();
//        message.setSender(sender);
//        message.setRecipient(recipient);
//        message.setContent(content);
//        message.setTimestamp(LocalDateTime.now());
//
//        return messageRepository.save(message); // Сохраните сообщение в базе данных
//    }
//
//    public List<Message> getChatHistory(Long senderId, Long recipientId) {
//        return messageRepository.findChatHistory(senderId, recipientId);
//    }
//
//    public List<User> getUserChats(Long userId) {
//        User currentUser = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        // Находим все сообщения, где пользователь является либо отправителем, либо получателем
//        List<Message> userMessages = messageRepository.findBySenderOrRecipient(currentUser, currentUser);
//
//        // Получаем уникальных пользователей, с которыми были чаты
//        return userMessages.stream()
//                .map(message -> message.getSender().getId().equals(userId) ? message.getRecipient() : message.getSender())
//                .distinct()
//                .collect(Collectors.toList());
//    }
//
//
//    /////////
//
//
//
//
//}
