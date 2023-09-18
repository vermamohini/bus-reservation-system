package com.tcs.bookingms.service;

import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_BOOKING_NOT_FOUND;
import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_BUS_NOT_FOUND;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tcs.bookingms.entities.BookingDetails;
import com.tcs.bookingms.entities.BookingStatus;
import com.tcs.bookingms.entities.BookingStatusEnum;
import com.tcs.bookingms.entities.BusRoute;
import com.tcs.bookingms.entities.PassengerDetails;
import com.tcs.bookingms.exceptions.EntityNotFoundException;
import com.tcs.bookingms.repository.BookingDetailsRepository;
import com.tcs.bookingms.repository.BookingStatusRepository;
import com.tcs.bookingms.repository.BusRouteRepository;
import com.tcs.bookingms.repository.PassengerDetailsRepository;
import com.tcs.bookingms.vo.BookingVo;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService implements RabbitListenerConfigurer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookingService.class);
	
	private final BookingDetailsRepository bookingDetailsRepository;
	
	private final BookingStatusRepository bookingStatusRepository;
	
	private final PassengerDetailsRepository passengerDetailsRepository;
	
	private final BusRouteRepository busRouteRepository;
	
	private final RabbitTemplate rabbitTemplate;
	
	@Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }
	
	@Value("${spring.rabbitmq.exchange}")
	private String exchange;
	
	@Value("${spring.rabbitmq.routingkey.bookingpending}")
	private String routingKey;
	
	@Value("${spring.rabbitmq.queue.inventorydebited}")
	private String inventoryDebitedQueue;
	
	public void saveBooking(BookingDetails bookingDetails) {
		String busNumber = bookingDetails.getBusNumber();
		BusRoute busRoute = busRouteRepository.findById(busNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BUS_NOT_FOUND + busNumber));
		
		LOGGER.info("Creating new booking for bus number: {}", busNumber);
		bookingDetails.setBookingDate(new Timestamp(System.currentTimeMillis()));
		bookingDetailsRepository.save(bookingDetails);
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.PENDING.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);	
		
		Double pricePerTicket = busRoute.getPricePerTicket();
		
		BookingVo bookingVo = new BookingVo();
		bookingVo.setBusNumber(busNumber);
		bookingVo.setBookingNumber(bookingDetails.getBookingNumber());
		bookingVo.setAmount(pricePerTicket * bookingDetails.getNoOfSeats());
		bookingVo.setNoOfSeats(bookingDetails.getNoOfSeats());
		
		LOGGER.info ("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKey, bookingVo);
		
		rabbitTemplate.convertAndSend(exchange, routingKey, bookingVo);
	}
	
	public void cancelBooking(Integer bookingNumber) {
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
		
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.CANCELLED.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);
	}
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.inventorydebited}")
	public void confirmBooking(BookingVo bookingVo) {
		
		LOGGER.info ("Received message: {} from event queue: {}", bookingVo, inventoryDebitedQueue);
		
		Integer bookingNumber = bookingVo.getBookingNumber();
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
		
		LOGGER.info ("Confirming booking for booking number: {}", bookingNumber); 
		
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.CONFIRMED.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);
		
		int bookedSeats = bookingVo.getNoOfSeats();
		List<PassengerDetails> passengerDetailsList = new ArrayList<PassengerDetails>();
		
		for (int index = 0; index < bookedSeats ; index++) {
			PassengerDetails passengerDetails = new PassengerDetails();
			passengerDetails.setBookingNumber(bookingNumber);
			passengerDetailsList.add(passengerDetails);
			
		}
		passengerDetailsRepository.saveAll(passengerDetailsList);
		
	}

}
