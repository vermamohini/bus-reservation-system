package com.tcs.bookingms.controller;

import static com.tcs.bookingms.constants.ErrorConstants.ERR_INSUFFICIENT_INVENTORY;
import static com.tcs.bookingms.constants.ErrorConstants.ERR_INSUFFICIENT_INVENTORY_CONTD;
import static com.tcs.bookingms.constants.MessageConstants.CANCEL_SUCCESS;
import static com.tcs.bookingms.constants.MessageConstants.CONFIRM_SUCCESS;
import static com.tcs.bookingms.constants.MessageConstants.SAVE_SUCCESS;
import static com.tcs.bookingms.constants.MessageConstants.SAVE_SUCCESS_CONTD;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.tcs.bookingms.vo.BookingVo;
import com.tcs.bookingms.vo.FullBookingDetailsVo;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);
	
	private final BusInventoryProxy busInventoryProxy;
	
	private final BookingService bookingService;
	
	@Retry(name = "inventory-api", fallbackMethod = "fallbackDefaultInventory")
	@GetMapping("/getInventory/{busNumber}")
	public Integer getAvailableInventory(@PathVariable String busNumber) {
		LOGGER.info("Calling inventory-api for busNumber: {}", busNumber);
		ResponseEntity<Integer> inventory = busInventoryProxy.getAvailableSeatsByBusNumber(busNumber);
		return inventory.getBody();
	}
	
	public Integer fallbackDefaultInventory(FeignException ex) {
		int defaultInventory = 0;
		LOGGER.info("Returning fallback default Inventory: {}", defaultInventory);
		return defaultInventory;
	}
	
	@PostMapping("/saveBooking")
	public ResponseEntity<String> saveBooking(@RequestBody BookingDetails bookingDetails) {
		bookingService.saveBooking(bookingDetails);
		return ResponseEntity.ok().body(SAVE_SUCCESS + bookingDetails.getBusNumber() + SAVE_SUCCESS_CONTD + bookingDetails.getBookingNumber());
	}
	
	@GetMapping("/getBooking/busNumber/{busNumber}")
	public List<FullBookingDetailsVo> getBookingByBusNumber(@PathVariable String busNumber) {
		return bookingService.getBookingByBusNumber(busNumber);
	}
	
	@GetMapping("/getBooking/bookingNumber/{bookingNumber}")
	public FullBookingDetailsVo getBookingByBookingNumber(@PathVariable Integer bookingNumber) {
		return bookingService.getBookingByBookingNumber(bookingNumber);
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
	
	@PostMapping("/cancelBooking")
	public ResponseEntity<String> cancelBooking(@RequestBody BookingVo bookingVo) {
		bookingService.cancelBooking(bookingVo);
		return ResponseEntity.ok().body(CANCEL_SUCCESS + bookingVo.getBookingNumber());
	}
	
	@PostMapping("/confirmBooking")
	public ResponseEntity<String> confirmBooking(@RequestBody BookingVo bookingVo) {
		bookingService.confirmBooking(bookingVo);
		return ResponseEntity.ok().body(CONFIRM_SUCCESS + bookingVo.getBookingNumber());
	}

}
