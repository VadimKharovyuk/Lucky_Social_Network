package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.MessageDTO;
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

    public void sendChatMessage(MessageDTO messageDTO) {
        try {
            log.info("Attempting to send message to RabbitMQ. Exchange: {}, RoutingKey: {}",
                    messagesExchange, chatRoutingKey);

            rabbitTemplate.convertAndSend(messagesExchange, chatRoutingKey, messageDTO);

            log.info("Successfully sent message to RabbitMQ: sender={}, content={}",
                    messageDTO.getSenderId(),
                    messageDTO.getContent());
        } catch (Exception e) {
            log.error("Error sending message to RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message to RabbitMQ", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.chat}")
    public void receiveChatMessage(MessageDTO messageDTO) {
        log.info("Received message from RabbitMQ queue. Message: {}", messageDTO);
        try {
            processMessage(messageDTO);
        } catch (Exception e) {
            log.error("Error processing RabbitMQ message: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void processMessage(MessageDTO messageDTO) {
        log.info("Processing message: sender={}, content={}, timestamp={}",
                messageDTO.getSenderId(),
                messageDTO.getContent(),
                messageDTO.getTimestamp());

        // Здесь можно добавить дополнительную логику обработки сообщения
        // Например, отправку уведомлений, сохранение в базу данных и т.д.
    }
}