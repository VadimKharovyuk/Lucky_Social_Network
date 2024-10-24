package com.example.lucky_social_network.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public Timer.Builder messageProcessingTimeBuilder() {
        return Timer.builder("message.processing.time")
                .description("Time taken to process messages")
                .tag("type", "chat");
    }

    @Bean
    public Timer messageSendTimer(MeterRegistry registry) {
        return Timer.builder("chat.message.send")
                .description("Time taken to send a message")
                .register(registry);
    }

    @Bean
    public Counter deletedMessagesCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.messages.deleted")
                .description("Number of deleted messages")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.messages.error")
                .description("Number of errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter messagesSentCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.messages.sent")
                .description("Number of sent messages")
                .register(meterRegistry);
    }
}