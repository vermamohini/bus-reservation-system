package com.tcs.bookingms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.bookingms.entities.BookingDetails;

public interface BookingDetailsRepository extends JpaRepository<BookingDetails, Integer> {

}
