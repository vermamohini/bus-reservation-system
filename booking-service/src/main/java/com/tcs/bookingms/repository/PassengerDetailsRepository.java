package com.tcs.bookingms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.bookingms.entities.PassengerDetails;

public interface PassengerDetailsRepository extends JpaRepository<PassengerDetails, Integer> {
	public List<PassengerDetails> findByBookingNumber(Integer bookingNumber);
}
