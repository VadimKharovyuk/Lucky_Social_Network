package com.example.lucky_social_network.websocket;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.websocket.ChatWebSocketHandler;
import com.example.lucky_social_network.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> userSessions = new HashMap<>();
    private final MessageRepository messageRepository;



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Получаем ID пользователя, который подключается к WebSocket
        Long userId = getUserIdFromSession(session);
        userSessions.put(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        // Пример формата сообщения: "recipientId:message_content"
        String payload = textMessage.getPayload();
        String[] parts = payload.split(":", 2);
        Long recipientId = Long.parseLong(parts[0]);
        String content = parts[1];

        Long senderId = getUserIdFromSession(session);
        System.out.println("User " + senderId + " sent message to " + recipientId + ": " + content);


        // Создаем и сохраняем сообщение в базе данных
        Message message = new Message();
        message.setSender(new User(senderId));  // Передаем только id
        message.setRecipient(new User(recipientId));  // Передаем только id
        message.setContent(content);  // Содержимое сообщения
        message.setTimestamp(LocalDateTime.now());  // Текущая дата и время
        messageRepository.save(message);

        // Отправляем сообщение получателю
        WebSocketSession recipientSession = userSessions.get(recipientId);
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage("Message from user " + senderId + ": " + content));
            System.out.println("Message sent to user " + recipientId);
        } else {
            System.out.println("User " + recipientId + " is not connected.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        userSessions.remove(userId);
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        // Предположим, что userId передается как параметр в URI, например: /chat?userId=1
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId")) {
            return Long.parseLong(query.split("=")[1]); // получаем userId
        } else {
            throw new IllegalArgumentException("User ID не найден в URI WebSocket сессии");
        }
    }
}