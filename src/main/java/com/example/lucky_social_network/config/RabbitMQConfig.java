package com.example.lucky_social_network.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.messages}")
    private String messagesExchange;

    @Value("${rabbitmq.queue.chat}")
    private String chatQueue;

    @Value("${rabbitmq.queue.notifications}")
    private String notificationsQueue;

    @Value("${rabbitmq.routing.key.chat}")
    private String chatRoutingKey;

    @Value("${rabbitmq.routing.key.notifications}")
    private String notificationsRoutingKey;

    @Bean
    public Queue chatQueue() {
        return new Queue(chatQueue, true);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(notificationsQueue, true);
    }

    @Bean
    public DirectExchange messagesExchange() {
        return new DirectExchange(messagesExchange);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, DirectExchange messagesExchange) {
        return BindingBuilder
                .bind(chatQueue)
                .to(messagesExchange)
                .with(chatRoutingKey);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, DirectExchange messagesExchange) {
        return BindingBuilder
                .bind(notificationsQueue)
                .to(messagesExchange)
                .with(notificationsRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}