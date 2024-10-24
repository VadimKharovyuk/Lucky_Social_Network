package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.MessageDTO;
import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.MessageRepository;
import com.example.lucky_social_network.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final RabbitMQService rabbitMQService;
    private final MeterRegistry meterRegistry;
    private final Timer messageSendTimer;

    /**
     * Удаление сообщений для пользователя 1
     */
    public void deleteMessage(Long messageId, Long userId, boolean forEveryone) {
        if (messageId == null || userId == null) {
            throw new IllegalArgumentException("Message ID and User ID cannot be null");
        }

        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            boolean isSender = message.getSender().getId().equals(userId);

            // Проверяем права на удаление для всех
            if (forEveryone && !isSender) {
                throw new RuntimeException("Only message sender can delete message for everyone");
            }

            // Проверяем, не удалено ли уже сообщение
            if ((isSender && message.isDeletedForSender()) ||
                    (!isSender && message.isDeletedForRecipient())) {
                throw new RuntimeException("Message already deleted");
            }

            if (forEveryone) {
                message.setDeletedForSender(true);
                message.setDeletedForRecipient(true);
            } else {
                if (isSender) {
                    message.setDeletedForSender(true);
                } else {
                    message.setDeletedForRecipient(true);
                }
            }

            message.setDeletedAt(LocalDateTime.now());
            messageRepository.save(message);

            // Отправляем уведомление через RabbitMQ
            sendDeleteNotification(message, forEveryone ? null : userId);

            log.info("Message deleted successfully. messageId={}, userId={}, forEveryone={}",
                    messageId, userId, forEveryone);

        } catch (Exception e) {
            log.error("Failed to delete message: messageId={}, userId={}, error={}",
                    messageId, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete message: " + e.getMessage(), e);
        }
    }

    /**
     * Удаление сообщений для пользователя всех
     */
    public void deleteMessagesForUser(Long userId, Long chatPartnerId, boolean forCurrentUserOnly) {
        if (userId == null || chatPartnerId == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }

        User currentUser = userService.getUserById(userId);
        User chatPartner = userService.getUserById(chatPartnerId);

        if (currentUser == null || chatPartner == null) {
            throw new RuntimeException("User not found");
        }

        try {
            List<Message> messages = messageRepository.findAvailableChatHistory(userId, chatPartnerId);

            if (messages.isEmpty()) {
                log.info("No messages found to delete between users {} and {}", userId, chatPartnerId);
                return;
            }

            int deletedCount = 0;
            for (Message message : messages) {
                boolean isSender = message.getSender().getId().equals(userId);

                // Проверяем, не удалено ли уже сообщение
                if ((isSender && message.isDeletedForSender()) ||
                        (!isSender && message.isDeletedForRecipient())) {
                    continue;
                }

                if (forCurrentUserOnly) {
                    if (isSender) {
                        message.setDeletedForSender(true);
                    } else {
                        message.setDeletedForRecipient(true);
                    }
                } else {
                    message.setDeletedForSender(true);
                    message.setDeletedForRecipient(true);
                }

                message.setDeletedAt(LocalDateTime.now());
                messageRepository.save(message);

                // Отправляем уведомление через RabbitMQ
                sendDeleteNotification(message, forCurrentUserOnly ? userId : null);

                deletedCount++;
                log.debug("Message {} marked as deleted for {}",
                        message.getId(),
                        forCurrentUserOnly ? "user " + userId : "both users");
            }

            log.info("Messages deleted successfully. userId={}, chatPartnerId={}, forCurrentUserOnly={}, deletedCount={}",
                    userId, chatPartnerId, forCurrentUserOnly, deletedCount);

        } catch (Exception e) {
            log.error("Failed to delete messages: userId={}, chatPartnerId={}, error={}",
                    userId, chatPartnerId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete messages: " + e.getMessage(), e);
        }
    }

    private void sendDeleteNotification(Message message, Long deletedForUserId) {
        try {
            MessageDTO deleteNotification = new MessageDTO(
                    "DELETE",
                    message.getId(),
                    MessageDTO.DeleteAction.CHAT_HISTORY,
                    deletedForUserId == null,
                    LocalDateTime.now().toString()
            );
            rabbitMQService.sendChatMessage(deleteNotification);
            log.debug("Delete notification sent for message {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send delete notification for message {}: {}",
                    message.getId(), e.getMessage(), e);
        }
    }


    public Message sendMessage(Long senderId, Long recipientId, String content) {
        return messageSendTimer.record(() -> {
            try {
                User sender = userService.getUserById(senderId);
                User recipient = userService.getUserById(recipientId);

                if (sender == null || recipient == null) {
                    meterRegistry.counter("chat.messages.error", "type", "user_not_found").increment();
                    throw new RuntimeException("Sender or recipient not found");
                }

                Message message = new Message();
                message.setSender(sender);
                message.setRecipient(recipient);
                message.setContent(content);
                message.setTimestamp(LocalDateTime.now());

                log.info("Saving message: from {} to {}: {}", senderId, recipientId, content);

                Message savedMessage = messageRepository.save(message);

                // Отправка в RabbitMQ
                MessageDTO messageDTO = new MessageDTO(
                        "CHAT",
                        savedMessage.getSender().getId(),
                        savedMessage.getContent(),
                        savedMessage.getTimestamp().toString()
                );
                rabbitMQService.sendChatMessage(messageDTO);

                // Увеличиваем счетчик успешных сообщений
                meterRegistry.counter("chat.messages.sent").increment();

                return savedMessage;
            } catch (Exception e) {
                // Увеличиваем счетчик ошибок
                meterRegistry.counter("chat.messages.error", "type", e.getClass().getSimpleName()).increment();
                throw e;
            }
        });
    }

    public List<Message> getChatHistory(Long senderId, Long recipientId) {
        List<Message> messages = messageRepository.findAvailableChatHistory(senderId, recipientId);

        return messages.stream()
                .filter(message -> {
                    boolean isSender = message.getSender().getId().equals(senderId);
                    // Не показываем сообщение, если оно удалено для текущего пользователя
                    return !(isSender ? message.isDeletedForSender() : message.isDeletedForRecipient());
                })
                .collect(Collectors.toList());
    }

//    public List<Message> getChatHistory(Long senderId, Long recipientId) {
//        return messageRepository.findAvailableChatHistory(senderId, recipientId);
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

