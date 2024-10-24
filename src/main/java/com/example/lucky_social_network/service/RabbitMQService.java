package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQService {

    private final AmqpTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.messages}")
    private String messagesExchange;

    @Value("${rabbitmq.routing.key.chat}")
    private String chatRoutingKey;

    public void sendChatMessage(Message message) {
        try {
            log.info("Attempting to send message to RabbitMQ. Exchange: {}, RoutingKey: {}",
                    messagesExchange, chatRoutingKey);

            rabbitTemplate.convertAndSend(messagesExchange, chatRoutingKey, message);

            log.info("Successfully sent message to RabbitMQ: sender={}, recipient={}, content={}",
                    message.getSender().getId(),
                    message.getRecipient().getId(),
                    message.getContent());
        } catch (Exception e) {
            log.error("Error sending message to RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message to RabbitMQ", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.chat}")
    public void receiveChatMessage(Message message) {
        log.info("Received message from RabbitMQ queue. Message: {}", message);
        try {
            processMessage(message);
        } catch (Exception e) {
            log.error("Error processing RabbitMQ message: {}", e.getMessage(), e);
            throw e; // Повторное выбрасывание исключения для обработки повторных попыток
        }
    }

    private void processMessage(Message message) {
        log.info("Processing message: sender={}, recipient={}, content={}",
                message.getSender().getId(),
                message.getRecipient().getId(),
                message.getContent());
    }
}