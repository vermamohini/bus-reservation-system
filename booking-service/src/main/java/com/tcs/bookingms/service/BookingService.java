package com.tcs.bookingms.service;

import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_BOOKING_NOT_FOUND;
import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_BOOKING_NOT_PENDING;
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
import com.tcs.bookingms.entities.FullBookingDetails;
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
	
	@Value("${spring.rabbitmq.routingkey.payment.process}")
	private String routingKeyPaymentProcess;
	
	@Value("${spring.rabbitmq.routingkey.payment.rollback}")
	private String routingKeyPaymentRollback;
	
	@Value("${spring.rabbitmq.routingkey.inventory.credit}")
	private String routingKeyInventoryCredit;
	
	@Value("${spring.rabbitmq.queue.booking.reject}")
	private String bookingRejectQueue;
	
	@Value("${spring.rabbitmq.queue.booking.confirm}")
	private String bookingConfirmQueue;
	
	
	public void saveBooking(BookingDetails bookingDetails) {
		String busNumber = bookingDetails.getBusNumber();
		BusRoute busRoute = busRouteRepository.findById(busNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BUS_NOT_FOUND + busNumber));
		
		LOGGER.info("Creating new booking for bus number: {}", busNumber);
		bookingDetails.setBookingDate(new Timestamp(System.currentTimeMillis()));
		bookingDetailsRepository.save(bookingDetails);
		
		String bookingStatusStr = BookingStatusEnum.PENDING.toString();
		LOGGER.info("Setting booking status for bus number: {} to {}", busNumber, bookingStatusStr);
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(bookingStatusStr);
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);	
		
		Double pricePerTicket = busRoute.getPricePerTicket();
		
		BookingVo bookingVo = new BookingVo();
		bookingVo.setBusNumber(busNumber);
		bookingVo.setBookingNumber(bookingDetails.getBookingNumber());
		bookingVo.setAmount(pricePerTicket * bookingDetails.getNoOfSeats());
		bookingVo.setNoOfSeats(bookingDetails.getNoOfSeats());
		
		LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyPaymentProcess, bookingVo);
		
		rabbitTemplate.convertAndSend(exchange, routingKeyPaymentProcess, bookingVo);
	}
	
	public void cancelBooking(BookingVo bookingVo) {
		Integer bookingNumber = bookingVo.getBookingNumber();
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
		
		LOGGER.info("Cancelling booking for booking number: {}", bookingNumber);
		
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.CANCELLED.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);
		
		triggerRollbackEvent(bookingVo);
	}
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.booking.confirm}")
	public void confirmBooking(BookingVo bookingVo) {
		
		try {
			LOGGER.info("Received message: {} from event queue: {}", bookingVo, bookingConfirmQueue);

			Integer bookingNumber = bookingVo.getBookingNumber();
			BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
					.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
			
			String pendingStatus = BookingStatusEnum.PENDING.toString();
			boolean bookingStatusOpen = bookingStatusRepository.findByBookingNumber(bookingNumber).stream().allMatch(obj -> obj.getBookingStatus().equals(pendingStatus));
			
			if (!bookingStatusOpen) {
				throw new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber + ERR_MSG_BOOKING_NOT_PENDING + pendingStatus);
			}

			LOGGER.info("Confirming booking for booking number: {}", bookingNumber);

			BookingStatus bookingStatus = new BookingStatus();
			bookingStatus.setBookingStatus(BookingStatusEnum.CONFIRMED.toString());
			bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
			bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			bookingStatusRepository.save(bookingStatus);

			LOGGER.info("Adding passenger details for booking number: {}", bookingNumber);

			int bookedSeats = bookingVo.getNoOfSeats();
			List<PassengerDetails> passengerDetailsList = new ArrayList<PassengerDetails>();

			for (int index = 0; index < bookedSeats; index++) {
				PassengerDetails passengerDetails = new PassengerDetails();
				passengerDetails.setBookingNumber(bookingNumber);
				passengerDetailsList.add(passengerDetails);

			}
			passengerDetailsRepository.saveAll(passengerDetailsList);
			
		} catch (final EntityNotFoundException ex) {
			LOGGER.info("Exception in Confirming booking for booking details: {}. Exception encountered : {}", bookingVo, ex);
			throw ex;
		}	catch (final Exception ex) {
			LOGGER.info("Exception in Confirming booking for booking details: {}. Exception encountered : {}", bookingVo, ex);
			
			rejectBookingStatus(bookingVo);	
			triggerRollbackEvent(bookingVo);
		}	
	}
	
	@RabbitListener(queues = "${spring.rabbitmq.queue.booking.reject}")
	public void rejectBooking(BookingVo bookingVo) {
		LOGGER.info("Received message: {} from event queue: {}", bookingVo, bookingRejectQueue);
		rejectBookingStatus(bookingVo);	
	}

	public void rejectBookingStatus(BookingVo bookingVo) {
		Integer bookingNumber = bookingVo.getBookingNumber();
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
		
		LOGGER.info("Rejecting booking for booking number: {}", bookingNumber);

		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.REJECTED.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);
	}
	
	public void triggerRollbackEvent(BookingVo bookingVo) {
		LOGGER.info("Triggering rollback of transactions for booking number: {}", bookingVo.getBookingNumber());
		
		LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyPaymentRollback, bookingVo);
		rabbitTemplate.convertAndSend(exchange, routingKeyPaymentRollback, bookingVo);
		
		LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyInventoryCredit, bookingVo);
		rabbitTemplate.convertAndSend(exchange, routingKeyInventoryCredit, bookingVo);	
	}
	
	public List<FullBookingDetails> getBookingByBusNumber(String busNumber) {
		LOGGER.info("Fetching bookings for bus number: {}", busNumber);
		List<BookingDetails> bookingDetailsList = bookingDetailsRepository.findByBusNumber(busNumber);
		
		List<FullBookingDetails> fullBookingDetailsList = new ArrayList<FullBookingDetails>();
		
		for (BookingDetails bookingDetails : bookingDetailsList) {
			FullBookingDetails fullBookingDetails = new FullBookingDetails();
			fullBookingDetails.setBookingDate(bookingDetails.getBookingDate());
			fullBookingDetails.setBookingNumber(bookingDetails.getBookingNumber());
			fullBookingDetails.setBookingStatus(bookingStatusRepository.findByBookingNumberOrderByCreatedDateDesc(bookingDetails.getBookingNumber()).get(0).getBookingStatus());
			fullBookingDetails.setBusNumber(busNumber);
			fullBookingDetails.setEndPoint(bookingDetails.getEndPoint());
			fullBookingDetails.setNoOfSeats(bookingDetails.getNoOfSeats());
			fullBookingDetails.setPassengerIds(passengerDetailsRepository.findByBookingNumber(bookingDetails.getBookingNumber()).stream().map(obj -> obj.getPassengerId()).toList());
			fullBookingDetails.setStartPoint(bookingDetails.getStartPoint());
			fullBookingDetailsList.add(fullBookingDetails);
		}
		return fullBookingDetailsList;
		
	}
	
	public FullBookingDetails getBookingByBookingNumber(Integer bookingNumber) {
		LOGGER.info("Fetching booking for booking number: {}", bookingNumber);
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
		
		FullBookingDetails fullBookingDetails = new FullBookingDetails();
		fullBookingDetails.setBookingDate(bookingDetails.getBookingDate());
		fullBookingDetails.setBookingNumber(bookingDetails.getBookingNumber());
		fullBookingDetails.setBookingStatus(bookingStatusRepository.findByBookingNumberOrderByCreatedDateDesc(bookingDetails.getBookingNumber()).get(0).getBookingStatus());
		fullBookingDetails.setBusNumber(bookingDetails.getBusNumber());
		fullBookingDetails.setEndPoint(bookingDetails.getEndPoint());
		fullBookingDetails.setNoOfSeats(bookingDetails.getNoOfSeats());
		fullBookingDetails.setPassengerIds(passengerDetailsRepository.findByBookingNumber(bookingDetails.getBookingNumber()).stream().map(obj -> obj.getPassengerId()).toList());
		fullBookingDetails.setStartPoint(bookingDetails.getStartPoint());
		return fullBookingDetails;
		
	}
}
