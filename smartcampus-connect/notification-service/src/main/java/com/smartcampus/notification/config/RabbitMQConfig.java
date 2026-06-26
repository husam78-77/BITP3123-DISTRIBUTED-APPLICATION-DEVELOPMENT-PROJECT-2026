// First Commit - Ahmed Abdulrahman Ahmed Ali Gamel - B032320114
// git commit -m "Add RabbitMQ configuration - Ahmed B032320114"
package com.smartcampus.notification.config;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String ENROLMENT_QUEUE    = "enrolment.queue";
    public static final String LIBRARY_QUEUE      = "library.queue";
    public static final String NOTIFICATION_EXCHANGE = "smartcampus.exchange";

    // Routing keys
    public static final String ENROLMENT_ROUTING_KEY = "enrolment.event";
    public static final String LIBRARY_ROUTING_KEY   = "library.event";

    // --- In-memory mock connection (no Docker needed) ---
    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(new MockConnectionFactory());
    }

    // --- Queues ---
    @Bean
    public Queue enrolmentQueue() {
        return new Queue(ENROLMENT_QUEUE, true);
    }

    @Bean
    public Queue libraryQueue() {
        return new Queue(LIBRARY_QUEUE, true);
    }

    // --- Exchange ---
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    // --- Bindings ---
    @Bean
    public Binding enrolmentBinding(Queue enrolmentQueue, TopicExchange exchange) {
        return BindingBuilder.bind(enrolmentQueue).to(exchange).with(ENROLMENT_ROUTING_KEY);
    }

    @Bean
    public Binding libraryBinding(Queue libraryQueue, TopicExchange exchange) {
        return BindingBuilder.bind(libraryQueue).to(exchange).with(LIBRARY_ROUTING_KEY);
    }

    // --- Message Converter (JSON) ---
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- RabbitTemplate ---
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}