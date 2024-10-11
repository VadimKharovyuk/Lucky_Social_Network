//package com.example.lucky_social_network.websocket;
//
//import com.example.lucky_social_network.model.Message;
//import com.example.lucky_social_network.model.User;
//import com.example.lucky_social_network.websocket.ChatWebSocketHandler;
//import com.example.lucky_social_network.repository.MessageRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class ChatWebSocketHandler extends TextWebSocketHandler {
//
//    private final Map<Long, WebSocketSession> userSessions = new HashMap<>();
//    private final MessageRepository messageRepository;
//
//
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // Получаем ID пользователя, который подключается к WebSocket
//        Long userId = getUserIdFromSession(session);
//        userSessions.put(userId, session);
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
//        // Пример формата сообщения: "recipientId:message_content"
//        String payload = textMessage.getPayload();
//        String[] parts = payload.split(":", 2);
//        Long recipientId = Long.parseLong(parts[0]);
//        String content = parts[1];
//
//        Long senderId = getUserIdFromSession(session);
//        System.out.println("User " + senderId + " sent message to " + recipientId + ": " + content);
//
//
//        // Создаем и сохраняем сообщение в базе данных
//        Message message = new Message();
//        message.setSender(new User(senderId));  // Передаем только id
//        message.setRecipient(new User(recipientId));  // Передаем только id
//        message.setContent(content);  // Содержимое сообщения
//        message.setTimestamp(LocalDateTime.now());  // Текущая дата и время
//        messageRepository.save(message);
//
//        // Отправляем сообщение получателю
//        WebSocketSession recipientSession = userSessions.get(recipientId);
//        if (recipientSession != null && recipientSession.isOpen()) {
//            recipientSession.sendMessage(new TextMessage("Message from user " + senderId + ": " + content));
//            System.out.println("Message sent to user " + recipientId);
//        } else {
//            System.out.println("User " + recipientId + " is not connected.");
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        Long userId = getUserIdFromSession(session);
//        userSessions.remove(userId);
//    }
//
//    private Long getUserIdFromSession(WebSocketSession session) {
//        // Предположим, что userId передается как параметр в URI, например: /chat?userId=1
//        String query = session.getUri().getQuery();
//        if (query != null && query.contains("userId")) {
//            return Long.parseLong(query.split("=")[1]); // получаем userId
//        } else {
//            throw new IllegalArgumentException("User ID не найден в URI WebSocket сессии");
//        }
//    }
//}

package com.example.lucky_social_network.websocket;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastActivityTimes = new ConcurrentHashMap<>();
    private final MessageRepository messageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        userSessions.put(userId, session);
        updateLastActivityTime(userId);
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

            Message message = new Message();
            message.setSender(new User(userId));
            message.setRecipient(new User(recipientId));
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());

            messageRepository.save(message);

            WebSocketSession recipientSession = userSessions.get(recipientId);
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.sendMessage(new TextMessage("Message from user " + userId + ": " + content));
                updateLastActivityTime(recipientId);
            }
        } catch (NumberFormatException e) {
            sendErrorMessage(session, "Invalid recipient ID");
        } catch (Exception e) {
            sendErrorMessage(session, "An error occurred while processing your message");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        userSessions.remove(userId);
        lastActivityTimes.remove(userId);
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
        session.sendMessage(new TextMessage("Error: " + errorMessage));
    }

    private void updateLastActivityTime(Long userId) {
        lastActivityTimes.put(userId, System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 60000) // Выполняется каждую минуту
    public void cleanupInactiveSessions() {
        long currentTime = System.currentTimeMillis();
        userSessions.entrySet().removeIf(entry -> {
            Long userId = entry.getKey();
            WebSocketSession session = entry.getValue();
            Long lastActivityTime = lastActivityTimes.get(userId);
            if (lastActivityTime == null) {
                return true; // Удаляем сессию, если нет информации о последней активности
            }
            boolean isInactive = !session.isOpen() || (currentTime - lastActivityTime > SESSION_TIMEOUT);
            if (isInactive) {
                lastActivityTimes.remove(userId);
            }
            return isInactive;
        });
    }
}