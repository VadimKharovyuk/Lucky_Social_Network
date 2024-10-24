package com.example.lucky_social_network.websocket;

import com.example.lucky_social_network.dto.MessageDTO;
import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {


    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastActivityTimes = new ConcurrentHashMap<>();
    private final MessageRepository messageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        userSessions.put(userId, session);
        updateLastActivityTime(userId);
        cleanupInactiveSessions();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = getUserIdFromSession(session);
        updateLastActivityTime(userId);

        try {
            String payload = textMessage.getPayload();
            if (payload.length() > MAX_MESSAGE_LENGTH) {
                sendErrorMessage(session, "Message is too long");
                return;
            }

            String[] parts = payload.split(":", 2);
            if (parts.length != 2) {
                sendErrorMessage(session, "Invalid message format");
                return;
            }

            Long recipientId = Long.parseLong(parts[0]);
            String content = parts[1];

            // Создаем и сохраняем сообщение
            Message message = new Message();
            message.setSender(new User(userId));
            message.setRecipient(new User(recipientId));
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());

            messageRepository.save(message);

            // Создаем DTO для отправки через WebSocket
            MessageDTO messageDTO = new MessageDTO(
                    "message",
                    userId,
                    content,
                    message.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            );

            // Отправляем сообщение получателю
            WebSocketSession recipientSession = userSessions.get(recipientId);
            if (recipientSession != null && recipientSession.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(messageDTO);
                recipientSession.sendMessage(new TextMessage(jsonMessage));
                updateLastActivityTime(recipientId);
            }
        } catch (NumberFormatException e) {
            sendErrorMessage(session, "Invalid recipient ID");
        } catch (Exception e) {
            log.error("Error processing message", e);
            sendErrorMessage(session, "An error occurred while processing your message");
        } finally {
            cleanupInactiveSessions();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        userSessions.remove(userId);
        lastActivityTimes.remove(userId);
        cleanupInactiveSessions();
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId")) {
            return Long.parseLong(query.split("=")[1]);
        } else {
            throw new IllegalArgumentException("User ID not found in WebSocket session URI");
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws IOException {
        MessageDTO errorDTO = new MessageDTO("error", null, errorMessage, null);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorDTO)));
    }

    private void updateLastActivityTime(Long userId) {
        lastActivityTimes.put(userId, System.currentTimeMillis());
    }

    private void cleanupInactiveSessions() {
        long currentTime = System.currentTimeMillis();
        userSessions.entrySet().removeIf(entry -> {
            Long userId = entry.getKey();
            WebSocketSession session = entry.getValue();
            boolean shouldRemove = false;

            try {
                Long lastActivityTime = lastActivityTimes.get(userId);
                if (lastActivityTime == null || (currentTime - lastActivityTime > SESSION_TIMEOUT)) {
                    shouldRemove = true;
                    lastActivityTimes.remove(userId);
                }
            } catch (Exception e) {
                log.error("Error while checking session for user {}: {}", userId, e.getMessage());
                shouldRemove = true;
            } finally {
                if (shouldRemove && session.isOpen()) {
                    try {
                        session.close();
                    } catch (IOException e) {
                        log.error("Error while closing session for user {}: {}", userId, e.getMessage());
                    }
                }
            }

            return shouldRemove;
        });
    }
}


//    private static final int MAX_MESSAGE_LENGTH = 1000;
//    private static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
//
//    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<Long, Long> lastActivityTimes = new ConcurrentHashMap<>();
//    private final MessageRepository messageRepository;
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        Long userId = getUserIdFromSession(session);
//        userSessions.put(userId, session);
//        updateLastActivityTime(userId);
//        cleanupInactiveSessions();
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
//        Long userId = getUserIdFromSession(session);
//        updateLastActivityTime(userId);
//
//        try {
//            String payload = textMessage.getPayload();
//            if (payload.length() > MAX_MESSAGE_LENGTH) {
//                sendErrorMessage(session, "Message is too long");
//                return;
//            }
//
//            String[] parts = payload.split(":", 2);
//            if (parts.length != 2) {
//                sendErrorMessage(session, "Invalid message format");
//                return;
//            }
//
//            Long recipientId = Long.parseLong(parts[0]);
//            String content = parts[1];
//
//            Message message = new Message();
//            message.setSender(new User(userId));
//            message.setRecipient(new User(recipientId));
//            message.setContent(content);
//            message.setTimestamp(LocalDateTime.now());
//
//            messageRepository.save(message);
//
//            WebSocketSession recipientSession = userSessions.get(recipientId);
//            if (recipientSession != null && recipientSession.isOpen()) {
//                recipientSession.sendMessage(new TextMessage("Message from user " + userId + ": " + content));
//                updateLastActivityTime(recipientId);
//            }
//        } catch (NumberFormatException e) {
//            sendErrorMessage(session, "Invalid recipient ID");
//        } catch (Exception e) {
//            sendErrorMessage(session, "An error occurred while processing your message");
//        } finally {
//            cleanupInactiveSessions();
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        Long userId = getUserIdFromSession(session);
//        userSessions.remove(userId);
//        lastActivityTimes.remove(userId);
//        cleanupInactiveSessions();
//    }
//
//    private Long getUserIdFromSession(WebSocketSession session) {
//        String query = session.getUri().getQuery();
//        if (query != null && query.contains("userId")) {
//            return Long.parseLong(query.split("=")[1]);
//        } else {
//            throw new IllegalArgumentException("User ID not found in WebSocket session URI");
//        }
//    }
//
//    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws IOException, IOException {
//        session.sendMessage(new TextMessage("Error: " + errorMessage));
//    }
//
//    private void updateLastActivityTime(Long userId) {
//        lastActivityTimes.put(userId, System.currentTimeMillis());
//    }
//
//    private void cleanupInactiveSessions() {
//        long currentTime = System.currentTimeMillis();
//        userSessions.entrySet().removeIf(entry -> {
//            Long userId = entry.getKey();
//            WebSocketSession session = entry.getValue();
//            boolean shouldRemove = false;
//
//            try {
//                Long lastActivityTime = lastActivityTimes.get(userId);
//                if (lastActivityTime == null || (currentTime - lastActivityTime > SESSION_TIMEOUT)) {
//                    shouldRemove = true;
//                    lastActivityTimes.remove(userId);
//                }
//            } catch (Exception e) {
//                log.error("Error while checking session for user {}: {}", userId, e.getMessage());
//                shouldRemove = true;
//            } finally {
//                if (shouldRemove && session.isOpen()) {
//                    try {
//                        session.close();
//                    } catch (IOException e) {
//                        log.error("Error while closing session for user {}: {}", userId, e.getMessage());
//                    }
//                }
//            }
//
//            return shouldRemove;
//        });
//    }
