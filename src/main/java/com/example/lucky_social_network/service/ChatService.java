package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.MessageRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Message sendMessage(Long senderId, Long recipientId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message); // Сохраните сообщение в базе данных
    }

    public List<Message> getChatHistory(Long senderId, Long recipientId) {
        return messageRepository.findChatHistory(senderId, recipientId);
    }
}
