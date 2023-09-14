package com.tcs.bookingms.service;

import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_BOOKING_NOT_FOUND;
import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_BUS_NOT_FOUND;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import com.tcs.bookingms.vo.PaymentVo;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {
	
	private final BookingDetailsRepository bookingDetailsRepository;
	
	private final BookingStatusRepository bookingStatusRepository;
	
	private final PassengerDetailsRepository passengerDetailsRepository;
	
	private final BusRouteRepository busRouteRepository;
	
	private final RabbitTemplate rabbitTemplate;
	
	@Value("${spring.rabbitmq.exchange}")
	private String exchange;
	

	@Value("${spring.rabbitmq.routingkey}")
	private String routingKey;
	
	public void saveBooking(BookingDetails bookingDetails) {
		bookingDetails.setBookingDate(new Timestamp(System.currentTimeMillis()));
		bookingDetailsRepository.save(bookingDetails);
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.PENDING.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatusRepository.save(bookingStatus);
		
		PaymentVo paymentVo = new PaymentVo();
		paymentVo.setBookingNumber(bookingDetails.getBookingNumber());
		String busNumber = bookingDetails.getBusNumber();
		BusRoute busRoute = busRouteRepository.findById(busNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BUS_NOT_FOUND + busNumber));
		Double pricePerTicket = busRoute.getPricePerTicket();
		paymentVo.setAmount(pricePerTicket * bookingDetails.getNoOfSeats());
		rabbitTemplate.convertAndSend(exchange, routingKey, paymentVo);
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
	
	public void confirmBooking(Integer bookingNumber) {
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new EntityNotFoundException(ERR_MSG_BOOKING_NOT_FOUND + bookingNumber));
		
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.CONFIRMED.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);
		
		int bookedSeats = bookingDetails.getNoOfSeats();
		List<PassengerDetails> passengerDetailsList = new ArrayList<PassengerDetails>();
		
		for (int index = 0; index < bookedSeats ; index++) {
			PassengerDetails passengerDetails = new PassengerDetails();
			passengerDetails.setBookingNumber(bookingNumber);
			passengerDetailsList.add(passengerDetails);
			
		}
		passengerDetailsRepository.saveAll(passengerDetailsList);
		
	}

}
