package com.bancolas.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "loan.exchange";
    public static final String AUDIT_QUEUE = "loan.audit.queue";
    public static final String ROUTING_KEY_EVALUATED = "loan.evaluated.#";
    public static final String NOTIFICATION_QUEUE = "loan.notification.queue";

    @Bean
    public TopicExchange loanExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange loanExchange) {
        return BindingBuilder
                .bind(auditQueue)
                .to(loanExchange)
                .with(ROUTING_KEY_EVALUATED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange loanExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(loanExchange)
                .with(ROUTING_KEY_EVALUATED);
    }
}