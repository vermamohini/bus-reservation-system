package com.tcs.bookingms.service;

import static com.tcs.bookingms.constants.ErrorConstants.ERR_MSG_NOT_FOUND;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.tcs.bookingms.entities.BookingDetails;
import com.tcs.bookingms.entities.BookingStatus;
import com.tcs.bookingms.entities.BookingStatusEnum;
import com.tcs.bookingms.entities.PassengerDetails;
import com.tcs.bookingms.exceptions.BookingNotFoundException;
import com.tcs.bookingms.repository.BookingDetailsRepository;
import com.tcs.bookingms.repository.BookingStatusRepository;
import com.tcs.bookingms.repository.PassengerDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {
	
	private final BookingDetailsRepository bookingDetailsRepository;
	
	private final BookingStatusRepository bookingStatusRepository;
	
	private final PassengerDetailsRepository passengerDetailsRepository;
	
	public void saveBooking(BookingDetails bookingDetails) {
		bookingDetails.setBookingDate(new Timestamp(System.currentTimeMillis()));
		bookingDetailsRepository.save(bookingDetails);
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.PENDING.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatusRepository.save(bookingStatus);
		
	}
	
	public void cancelBooking(Integer bookingNumber) {
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new BookingNotFoundException(ERR_MSG_NOT_FOUND + bookingNumber));
		
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatus(BookingStatusEnum.CANCELLED.toString());
		bookingStatus.setBookingNumber(bookingDetails.getBookingNumber());
		bookingStatus.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		bookingStatusRepository.save(bookingStatus);
	}
	
	public void confirmBooking(Integer bookingNumber) {
		BookingDetails bookingDetails = bookingDetailsRepository.findById(bookingNumber)
				.orElseThrow(() -> new BookingNotFoundException(ERR_MSG_NOT_FOUND + bookingNumber));
		
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
