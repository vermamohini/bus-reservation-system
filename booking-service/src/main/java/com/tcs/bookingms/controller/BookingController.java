package com.tcs.bookingms.controller;

import static com.tcs.bookingms.constants.ErrorConstants.ERR_INSUFFICIENT_INVENTORY;
import static com.tcs.bookingms.constants.ErrorConstants.ERR_INSUFFICIENT_INVENTORY_CONTD;
import static com.tcs.bookingms.constants.MessageConstants.SAVE_SUCCESS;
import static com.tcs.bookingms.constants.MessageConstants.SAVE_SUCCESS_CONTD;
import static com.tcs.bookingms.constants.MessageConstants.UPDATE_SUCCESS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.bookingms.entities.BookingDetails;
import com.tcs.bookingms.exceptions.InventoryException;
import com.tcs.bookingms.proxies.BusInventoryProxy;
import com.tcs.bookingms.service.BookingService;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
	
	private Logger logger = LoggerFactory.getLogger(BookingController.class);
	
	private final BusInventoryProxy busInventoryProxy;
	
	private final BookingService bookingService;
	
	@Retry(name = "inventory-api", fallbackMethod = "fallbackDefaultInventory")
	@GetMapping("/getInventory/{busNumber}")
	public Integer getAvailableInventory(@PathVariable String busNumber) {
		try {
			logger.info("Calling inventory-api for busNumber: {}", busNumber);
			ResponseEntity<Integer> inventory = busInventoryProxy.getAvailableSeatsByBusNumber(busNumber);
			return inventory.getBody();
		} catch (FeignException e) {
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				throw new InventoryException(e.getMessage());
			}
			throw e;
		}
	}
	
	public Integer fallbackDefaultInventory(FeignException ex) {
		int defaultInventory = 0;
		logger.info("Returning fallback default Inventory: {}", defaultInventory);
		return defaultInventory;
	}
	
	@PostMapping("/saveBooking")
	public ResponseEntity<String> saveBooking(@RequestBody BookingDetails bookingDetails) {
		bookingService.saveBooking(bookingDetails);
		return ResponseEntity.ok().body(SAVE_SUCCESS + bookingDetails.getBusNumber() + SAVE_SUCCESS_CONTD + bookingDetails.getBookingNumber());
	}
	
	@PostMapping("/checkInventoryAndSaveBooking")
	public ResponseEntity<String> checkInventoryAndSaveBooking(@RequestBody BookingDetails bookingDetails) {
		
		Integer availableSeats = getAvailableInventory(bookingDetails.getBusNumber());
		Integer reqdSeats = bookingDetails.getNoOfSeats();
		if (availableSeats >= reqdSeats) {
			return saveBooking(bookingDetails);
		} else {
			throw new InventoryException(ERR_INSUFFICIENT_INVENTORY + availableSeats + ERR_INSUFFICIENT_INVENTORY_CONTD + reqdSeats);
		}
		
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
