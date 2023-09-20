package com.tcs.inventoryms.service;

import static com.tcs.inventoryms.constants.ErrorConstants.ERR_INSUFFICIENT_INVENTORY;
import static com.tcs.inventoryms.constants.ErrorConstants.ERR_INSUFFICIENT_INVENTORY_CONTD;
import static com.tcs.inventoryms.constants.ErrorConstants.ERR_INVALID_NO_OF_SEATS;
import static com.tcs.inventoryms.constants.ErrorConstants.ERR_MSG_NOT_FOUND;

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

import com.tcs.inventoryms.entities.BusInventory;
import com.tcs.inventoryms.entities.InventoryOperationEnum;
import com.tcs.inventoryms.entities.InventoryUpdateLog;
import com.tcs.inventoryms.exceptions.BusInventoryNotFoundException;
import com.tcs.inventoryms.exceptions.InvalidNoOfSeatsException;
import com.tcs.inventoryms.exceptions.InventoryException;
import com.tcs.inventoryms.repository.BusInventoryRepository;
import com.tcs.inventoryms.repository.InventoryUpdateLogRepository;
import com.tcs.inventoryms.vo.BookingVo;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class InventoryService implements RabbitListenerConfigurer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);
	
	private final BusInventoryRepository busInventoryRepository;
	
	private final InventoryUpdateLogRepository inventoryUpdateLogRepository;
	
	
	private final RabbitTemplate rabbitTemplate;
	
	@Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }
	
	@Value("${spring.rabbitmq.exchange}")
	private String exchange;
	
	@Value("${spring.rabbitmq.routingkey.booking.confirm}")
	private String routingKeyBookingConfirm;
	
	@Value("${spring.rabbitmq.routingkey.booking.reject}")
	private String routingKeyBookingReject;
	
	@Value("${spring.rabbitmq.routingkey.payment.rollback}")
	private String routingKeyPaymentRollback;
	
	@Value("${spring.rabbitmq.queue.inventory.debit}")
	private String inventoryDebitQueue;
	
	@Value("${spring.rabbitmq.queue.inventory.creditt}")
	private String inventoryCreditQueue;
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.inventory.debit}")
	public void reducefromBusInventoryByBusNumber(BookingVo bookingVo) {
		try {
			LOGGER.info ("Received message: {} from event queue: {}", bookingVo, inventoryDebitQueue);
			
			Integer noOfSeats = bookingVo.getNoOfSeats();
			if (noOfSeats == null) {
				new InvalidNoOfSeatsException(ERR_INVALID_NO_OF_SEATS);
			}
			
			String busNumber = bookingVo.getBusNumber();
			BusInventory existingInventory = validateExistingInventory(busNumber, noOfSeats);
			
			existingInventory.setAvailableSeats(existingInventory.getAvailableSeats() - noOfSeats);
			existingInventory.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
			busInventoryRepository.save(existingInventory);
			
			InventoryUpdateLog inventoryLog = new InventoryUpdateLog();
			inventoryLog.setBookingNumber(bookingVo.getBookingNumber());
			inventoryLog.setNoOfSeats(noOfSeats);
			inventoryLog.setOperation(InventoryOperationEnum.DEBIT.toString());
			inventoryLog.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			inventoryUpdateLogRepository.save(inventoryLog);
			
			LOGGER.info ("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyBookingConfirm, bookingVo);
			rabbitTemplate.convertAndSend(exchange, routingKeyBookingConfirm, bookingVo);
			
		} catch(final Exception ex) {
			LOGGER.info("Exception in Debiting inventory for booking details: {}. Exception encountered : {}", bookingVo, ex);
			triggerRollbackEvent(bookingVo);
		}
		
	}
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.inventory.credit}")
	public void addToBusInventoryByBusNumber(BookingVo bookingVo) {
		LOGGER.info("Received message: {} from event queue: {}", bookingVo, inventoryCreditQueue);
		Integer noOfSeats = bookingVo.getNoOfSeats();
		if (noOfSeats == null) {
			new InvalidNoOfSeatsException(ERR_INVALID_NO_OF_SEATS);
		}
		
		String busNumber = bookingVo.getBusNumber();
		BusInventory existingInventory = busInventoryRepository.findById(busNumber)
                .orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		
		existingInventory.setAvailableSeats(existingInventory.getAvailableSeats() + noOfSeats);
		existingInventory.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
		busInventoryRepository.save(existingInventory);
		
		InventoryUpdateLog inventoryLog = new InventoryUpdateLog();
		inventoryLog.setBookingNumber(bookingVo.getBookingNumber());
		inventoryLog.setNoOfSeats(noOfSeats);
		inventoryLog.setOperation(InventoryOperationEnum.CREDIT.toString());
		inventoryLog.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		inventoryUpdateLogRepository.save(inventoryLog);
	}
	
	public BusInventory validateExistingInventory(String busNumber, Integer noOfSeats) {
		BusInventory existingInventory = busInventoryRepository.findById(busNumber)
                .orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		if (existingInventory.getAvailableSeats() >= noOfSeats) {
			throw new InventoryException(ERR_INSUFFICIENT_INVENTORY + existingInventory.getAvailableSeats()  + ERR_INSUFFICIENT_INVENTORY_CONTD + noOfSeats);
		}
		return existingInventory;
	}
	
	public void triggerRollbackEvent(BookingVo bookingVo) {
		LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyPaymentRollback, bookingVo);
		rabbitTemplate.convertAndSend(exchange, routingKeyPaymentRollback, bookingVo);
		
		LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyBookingReject, bookingVo);
		rabbitTemplate.convertAndSend(exchange, routingKeyBookingReject, bookingVo);	
	}

}
