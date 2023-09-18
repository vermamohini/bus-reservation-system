package com.tcs.inventoryms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
	
    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    
	@Value("${spring.rabbitmq.queue.paymentprocessed}")
    private String paymentProcessedQueue;
    
    @Value("${spring.rabbitmq.routingkey.paymentprocessed}")
    private String routingKeyPaymentProcessedQueue;

	@Value("${spring.rabbitmq.queue.inventorydebited}")
    private String inventoryDebitedQueue;
    
    @Value("${spring.rabbitmq.routingkey.inventorydebited}")
    private String routingKeyInventoryDebitedQueue;
    
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.rabbitmq.host}")
    private String host;
    
    @Bean
    Queue queue() {
        return new Queue(paymentProcessedQueue, true);
    }
    
    @Bean
    Queue inventoryDebitedQueue() {
        return new Queue(inventoryDebitedQueue, true);
    }
    
    @Bean
    Exchange busRouteExchange() {
        return ExchangeBuilder.directExchange(exchange).durable(true).build();
    }
    
    @Bean
    Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(busRouteExchange())
                .with(routingKeyPaymentProcessedQueue)
                .noargs();
    }
    
    @Bean
    Binding inventoryDebitedBinding() {
        return BindingBuilder
                .bind(inventoryDebitedQueue())
                .to(busRouteExchange())
                .with(routingKeyInventoryDebitedQueue)
                .noargs();
    }
    
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        return cachingConnectionFactory;
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

}
