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
	
	@Value("${spring.rabbitmq.routingkey.inventory.debit}")
	private String routingKeyInventoryDebit;
	
	@Value("${spring.rabbitmq.routingkey.booking.reject}")
	private String routingKeyBookingReject;
	
	@Value("${spring.rabbitmq.queue.payment.process}")
	private String paymentProcessQueue;
	
	@Value("${spring.rabbitmq.queue.payment.rollback}")
	private String paymentRollbackQueue;
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.payment.process}")
	public void savePayment(BookingVo bookingVo) {
		
		try {
			LOGGER.info ("Received message: {} from event queue: {}", bookingVo, paymentProcessQueue);
			Integer bookingNumber = bookingVo.getBookingNumber();
			PaymentDetails existingPayment = paymentDetailsRepository.findByBookingNumber(bookingNumber);
			if (existingPayment == null) {
				LOGGER.info ("No existing Payment found for booking number: {}. Creating new Payment.", bookingNumber);
				PaymentDetails paymentDetails = new PaymentDetails();
				paymentDetails.setBookingNumber(bookingNumber);
				paymentDetails.setAmount(bookingVo.getAmount());
				paymentDetails.setPaymentDate(new Timestamp(System.currentTimeMillis()));
				paymentDetailsRepository.save(paymentDetails);
				
				String paymentStatusStr = PaymentStatusEnum.PROCESSED.toString();
				LOGGER.info("Setting payment status for booking number: {} to {}", bookingNumber, paymentStatusStr);
				PaymentStatus paymentStatus = new PaymentStatus();
				paymentStatus.setPaymentStatus(paymentStatusStr);
				paymentStatus.setPaymentNumber(paymentDetails.getPaymentNumber());
				paymentStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				paymentStatusRepository.save(paymentStatus);
				
				LOGGER.info ("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyInventoryDebit, bookingVo);
				rabbitTemplate.convertAndSend(exchange, routingKeyInventoryDebit, bookingVo);
				
			} else {
				throw new PaymentAlreadyExistsException(ERR_MSG_ALREADY_EXISTS + bookingVo.getBookingNumber());
			}
			
		} catch(final Exception ex) {
			LOGGER.info("Exception in Processing payment for booking details: {}. Exception encountered : {}", bookingVo, ex);
			triggerRollbackEvent(bookingVo);
		}
		
	}
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.payment.rollback}")
	public void refundPayment(BookingVo bookingVo) {
		LOGGER.info("Received message: {} from event queue: {}", bookingVo, paymentRollbackQueue);
		refundPaymentStatus(bookingVo);	
	}

	public void refundPaymentStatus(BookingVo bookingVo) {
		Integer bookingNumber = bookingVo.getBookingNumber();
		PaymentDetails existingPayment = paymentDetailsRepository.findByBookingNumber(bookingNumber);
		
		LOGGER.info("Refunding payment for booking number: {}", bookingVo.getBookingNumber());

		PaymentStatus paymentStatus = new PaymentStatus();
		paymentStatus.setPaymentStatus(PaymentStatusEnum.REFUNDED.toString());
		paymentStatus.setPaymentNumber(existingPayment.getPaymentNumber());
		paymentStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		paymentStatusRepository.save(paymentStatus);
	}
	
	public void triggerRollbackEvent(BookingVo bookingVo) {
		LOGGER.info("Triggering rollback of transactions for booking number: {}", bookingVo.getBookingNumber());
		
		LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyBookingReject, bookingVo);
		rabbitTemplate.convertAndSend(exchange, routingKeyBookingReject, bookingVo);	
	}

}
