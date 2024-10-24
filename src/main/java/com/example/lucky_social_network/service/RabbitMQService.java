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
            log.info("Message sent [time={}ms]: sender={}, recipientId={}, messageId={}",
                    processTime,
                    messageDTO.getSenderId(),
                    messageDTO.getContent(),
                    messageDTO.getTimestamp());

        } catch (Exception e) {
            log.error("Send failed: sender={}, error={}",
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
        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName()
                .replace("org.springframework.amqp.rabbit.RabbitListenerEndpointContainer", "RabbitListener");

        try {
            log.debug("{} - Processing started [thread={}]: sender={}",
                    listenerId,
                    threadName,
                    messageDTO.getSenderId());

            processMessage(messageDTO);

            long processTime = System.currentTimeMillis() - startTime;
            log.info("{} - Message processed [thread={}, time={}ms]: sender={}, content={}, length={}",
                    listenerId,
                    threadName,
                    processTime,
                    messageDTO.getSenderId(),
                    messageDTO.getContent(),
                    messageDTO.getContent().length());

        } catch (Exception e) {
            log.error("{} - Processing failed [thread={}]: sender={}, error={}",
                    listenerId,
                    threadName,
                    messageDTO.getSenderId(),
                    e.getMessage(), e);
            throw e;
        }
    }

    private void processMessage(MessageDTO messageDTO) {
        String threadName = Thread.currentThread().getName()
                .replace("org.springframework.amqp.rabbit.RabbitListenerEndpointContainer", "RabbitListener");

        log.debug("Processing started [thread={}]: sender={}",
                threadName,
                messageDTO.getSenderId());

        // Ваша логика обработки

        log.debug("Processing completed [thread={}]: sender={}",
                threadName,
                messageDTO.getSenderId());
    }
}