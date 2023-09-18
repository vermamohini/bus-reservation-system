package com.tcs.paymentms.service;

import static com.tcs.paymentms.constants.ErrorConstants.ERR_MSG_ALREADY_EXISTS;

import java.sql.Timestamp;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tcs.paymentms.entities.PaymentDetails;
import com.tcs.paymentms.entities.PaymentStatus;
import com.tcs.paymentms.entities.PaymentStatusEnum;
import com.tcs.paymentms.exceptions.PaymentAlreadyExistsException;
import com.tcs.paymentms.repository.PaymentDetailsRepository;
import com.tcs.paymentms.repository.PaymentStatusRepository;
import com.tcs.paymentms.vo.BookingVo;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class PaymentService implements RabbitListenerConfigurer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
	
	private final PaymentDetailsRepository paymentDetailsRepository;
	
	private final PaymentStatusRepository paymentStatusRepository;
	
	
	private final RabbitTemplate rabbitTemplate;
	
	@Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }
	
	@Value("${spring.rabbitmq.exchange}")
	private String exchange;
	
	@Value("${spring.rabbitmq.routingkey.paymentprocessed}")
	private String routingKey;
	
	@Value("${spring.rabbitmq.queue.bookingpending}")
	private String bookingPendingQueue;
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.bookingpending}")
	public void savePayment(BookingVo bookingVo) {
		
		LOGGER.info ("Received message: {} from event queue: {}", bookingVo, bookingPendingQueue);
		Integer bookingNumber = bookingVo.getBookingNumber();
		PaymentDetails existingPayment = paymentDetailsRepository.findByBookingNumber(bookingNumber);
		if (existingPayment == null) {
			LOGGER.info ("No existing Payment found for booking number: {}. Creating new Payment.", bookingNumber);
			PaymentDetails paymentDetails = new PaymentDetails();
			paymentDetails.setBookingNumber(bookingNumber);
			paymentDetails.setAmount(bookingVo.getAmount());
			paymentDetails.setPaymentDate(new Timestamp(System.currentTimeMillis()));
			paymentDetailsRepository.save(paymentDetails);
			
			PaymentStatus paymentStatus = new PaymentStatus();
			paymentStatus.setPaymentStatus(PaymentStatusEnum.PROCESSED.toString());
			paymentStatus.setPaymentNumber(paymentDetails.getPaymentNumber());
			paymentStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			paymentStatusRepository.save(paymentStatus);
			
			LOGGER.info ("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKey, bookingVo);
			rabbitTemplate.convertAndSend(exchange, routingKey, bookingVo);
			
		} else {
			throw new PaymentAlreadyExistsException(ERR_MSG_ALREADY_EXISTS + bookingVo.getBookingNumber());
		}
	}

}
