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
            long startTime = System.currentTimeMillis();

            rabbitTemplate.convertAndSend(messagesExchange, chatRoutingKey, messageDTO);

            long processTime = System.currentTimeMillis() - startTime;
            if ("CHAT".equals(messageDTO.getType())) {
                log.info("Message sent [time={}ms]: sender={}, content={}, timestamp={}",
                        processTime,
                        messageDTO.getSenderId(),
                        messageDTO.getContent(),
                        messageDTO.getTimestamp());
            } else {
                log.info("Delete notification sent [time={}ms]: messageId={}, deleteAction={}, forEveryone={}",
                        processTime,
                        messageDTO.getMessageId(),
                        messageDTO.getDeleteAction(),
                        messageDTO.isForEveryone());
            }

        } catch (Exception e) {
            log.error("Send failed: type={}, sender={}, error={}",
                    messageDTO.getType(),
                    messageDTO.getSenderId(),
                    e.getMessage(), e);
            throw new RuntimeException("Failed to send message", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.chat}", id = "listener1")
    public void receiveChatMessage1(MessageDTO messageDTO) {
        processMessageWithListener(messageDTO, "Listener-1");
    }

    @RabbitListener(queues = "${rabbitmq.queue.chat}", id = "listener2")
    public void receiveChatMessage2(MessageDTO messageDTO) {
        processMessageWithListener(messageDTO, "Listener-2");
    }

    private void processMessageWithListener(MessageDTO messageDTO, String listenerId) {
        try {
            String threadName = Thread.currentThread().getName()
                    .replace("org.springframework.amqp.rabbit.RabbitListenerEndpointContainer", "RabbitListener");

            if ("CHAT".equals(messageDTO.getType())) {
                log.info("{} - Chat message received [thread={}]: sender={}, content={}",
                        listenerId,
                        threadName,
                        messageDTO.getSenderId(),
                        messageDTO.getContent());
            } else if ("DELETE".equals(messageDTO.getType())) {
                log.info("{} - Delete notification received [thread={}]: messageId={}, action={}, forEveryone={}",
                        listenerId,
                        threadName,
                        messageDTO.getMessageId(),
                        messageDTO.getDeleteAction(),
                        messageDTO.isForEveryone());
            }

        } catch (Exception e) {
            log.error("{} - Processing failed: type={}, error={}",
                    listenerId,
                    messageDTO.getType(),
                    e.getMessage(), e);
            throw e;
        }
    }

}