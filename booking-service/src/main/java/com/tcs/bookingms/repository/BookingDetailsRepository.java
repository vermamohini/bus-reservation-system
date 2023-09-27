package com.tcs.bookingms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.bookingms.entities.BookingDetails;

public interface BookingDetailsRepository extends JpaRepository<BookingDetails, Integer> {
	
	public List<BookingDetails> findByBusNumber(String busNumber);

}
