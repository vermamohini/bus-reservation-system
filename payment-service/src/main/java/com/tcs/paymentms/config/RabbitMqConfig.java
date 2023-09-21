package com.tcs.paymentms.config;

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

	@Value("${spring.rabbitmq.queue.inventory.debit}")
	private String inventoryDebitQueue;

	@Value("${spring.rabbitmq.routingkey.inventory.debit}")
	private String routingKeyInventoryDebit;
    
	@Value("${spring.rabbitmq.queue.booking.reject}")
	private String bookingRejectQueue;

	@Value("${spring.rabbitmq.routingkey.booking.reject}")
	private String routingKeyBookingReject;

	@Value("${spring.rabbitmq.queue.payment.process}")
	private String paymentProcessQueue;

	@Value("${spring.rabbitmq.routingkey.payment.process}")
	private String routingKeyPaymentProcess;
	
	@Value("${spring.rabbitmq.queue.payment.rollback}")
	private String paymentRollbackQueue;

	@Value("${spring.rabbitmq.routingkey.payment.rollback}")
	private String routingKeyPaymentRollback;
    
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.rabbitmq.host}")
    private String host;
    
    @Bean
    Queue inventoryDebitQueue() {
        return new Queue(inventoryDebitQueue, true);
    }
    
    @Bean
    Queue bookingRejectQueue() {
        return new Queue(bookingRejectQueue, true);
    }
    
	@Bean
	Queue paymentProcessQueue() {
		return new Queue(paymentProcessQueue, true);
	}
    
	@Bean
	Queue paymentRollbackQueue() {
		return new Queue(paymentRollbackQueue, true);
	}
    
    @Bean
    Exchange busRouteExchange() {
        return ExchangeBuilder.directExchange(exchange).durable(true).build();
    }
    
    @Bean
    Binding inventoryDebitBinding() {
		return BindingBuilder.bind(inventoryDebitQueue()).to(busRouteExchange()).with(routingKeyInventoryDebit)
				.noargs();
    }
    
    @Bean
    Binding bookingRejectBinding() {
		return BindingBuilder.bind(bookingRejectQueue()).to(busRouteExchange()).with(routingKeyBookingReject)
				.noargs();
    }
    
	@Bean
	Binding paymentProcessBinding() {
		return BindingBuilder.bind(paymentProcessQueue()).to(busRouteExchange()).with(routingKeyPaymentProcess).noargs();
	}
	
	@Bean
	Binding paymentRollbackBinding() {
		return BindingBuilder.bind(paymentRollbackQueue()).to(busRouteExchange()).with(routingKeyPaymentRollback).noargs();
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
