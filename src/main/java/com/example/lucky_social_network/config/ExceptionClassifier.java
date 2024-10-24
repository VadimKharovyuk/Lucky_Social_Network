package com.example.lucky_social_network.config;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler.DefaultExceptionStrategy;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * Классификатор исключений для RabbitMQ
 */

public class ExceptionClassifier extends DefaultExceptionStrategy {

    @Override
    public boolean isFatal(Throwable t) {
        // Проверяем исключения при выполнении слушателя
        if (t instanceof ListenerExecutionFailedException) {
            Throwable cause = t.getCause();

            // Фатальные ошибки
            return cause instanceof MessageConversionException ||  // Ошибки конвертации сообщений
                    cause instanceof IllegalArgumentException ||    // Неверные аргументы
                    cause instanceof NullPointerException;         // NULL указатели
        }

        // Для остальных случаев используем стандартную стратегию
        return super.isFatal(t);
    }
}