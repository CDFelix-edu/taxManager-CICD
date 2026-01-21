package it.unimol.taxManager.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Exchange ricevuto da GestioneUtenti
    public static final String USERS_EXCHANGE = "users.exchange";

    // Routing keys (GestioneUtenti → taxManager)
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";
    public static final String ROLE_ASSIGNED = "role.assigned";

    // Queue per ricevere
    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(USER_CREATED).build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(USER_UPDATED).build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED).build();
    }

    @Bean
    public Queue roleAssignedQueue() {
        return QueueBuilder.durable(ROLE_ASSIGNED).build();
    }

    @Bean
    public TopicExchange usersExchange() {
        return new TopicExchange(USERS_EXCHANGE);
    }

    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange usersExchange) {
        return BindingBuilder.bind(userCreatedQueue).to(usersExchange).with(USER_CREATED);
    }

    @Bean
    public Binding userUpdatedBinding(Queue userUpdatedQueue, TopicExchange usersExchange) {
        return BindingBuilder.bind(userUpdatedQueue).to(usersExchange).with(USER_UPDATED);
    }

    @Bean
    public Binding userDeletedBinding(Queue userDeletedQueue, TopicExchange usersExchange) {
        return BindingBuilder.bind(userDeletedQueue).to(usersExchange).with(USER_DELETED);
    }

    @Bean
    public Binding roleAssignedBinding(Queue roleAssignedQueue, TopicExchange usersExchange) {
        return BindingBuilder.bind(roleAssignedQueue).to(usersExchange).with(ROLE_ASSIGNED);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}