package com.example.lucky_social_network.config;

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
}