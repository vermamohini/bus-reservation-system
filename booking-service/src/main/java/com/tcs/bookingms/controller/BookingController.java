package com.tcs.bookingms.controller;

import static com.tcs.bookingms.constants.MessageConstants.SAVE_SUCCESS;
import static com.tcs.bookingms.constants.MessageConstants.SAVE_SUCCESS_CONTD;
import static com.tcs.bookingms.constants.MessageConstants.UPDATE_SUCCESS;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.bookingms.entities.BookingDetails;
import com.tcs.bookingms.proxies.BusInventoryProxy;
import com.tcs.bookingms.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
	
	private final BusInventoryProxy busInventoryProxy;
	
	private final BookingService bookingService;
	
	@GetMapping("/getInventory/{busNumber}")
	public ResponseEntity<Integer> getAvailableInventory(@PathVariable String busNumber) {
		ResponseEntity<Integer> inventory = busInventoryProxy.getAvailableSeatsByBusNumber(busNumber);
		return ResponseEntity.ok().body(inventory.getBody());
	}
	
	@PostMapping("/saveBooking")
	public ResponseEntity<String> saveBooking(@RequestBody BookingDetails bookingDetails) {
		bookingService.saveBooking(bookingDetails);
		return ResponseEntity.ok().body(SAVE_SUCCESS + bookingDetails.getBusNumber() + SAVE_SUCCESS_CONTD + bookingDetails.getBookingNumber());
	}
	
	@PostMapping("/cancelBooking/{bookingNumber}")
	public ResponseEntity<String> cancelBooking(@PathVariable Integer bookingNumber) {
		bookingService.cancelBooking(bookingNumber);
		return ResponseEntity.ok().body(UPDATE_SUCCESS + bookingNumber);
	}
	
	@PostMapping("/confirmBooking/{bookingNumber}")
	public ResponseEntity<String> confirmBooking(@PathVariable Integer bookingNumber) {
		bookingService.confirmBooking(bookingNumber);
		return ResponseEntity.ok().body(UPDATE_SUCCESS + bookingNumber);
	}

}
