package com.tcs.bookingms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.bookingms.entities.BookingStatus;

public interface BookingStatusRepository extends JpaRepository<BookingStatus, Integer> {
	
	public List<BookingStatus> findByBookingNumber(Integer bookingNumber);

}
