package com.tcs.inventoryms.controller;

import static com.tcs.inventoryms.constants.ErrorConstants.ERR_INVALID_NO_OF_SEATS;
import static com.tcs.inventoryms.constants.ErrorConstants.ERR_MSG_ALREADY_EXISTS;
import static com.tcs.inventoryms.constants.ErrorConstants.ERR_MSG_NOT_FOUND;
import static com.tcs.inventoryms.constants.MessageConstants.DELETE_SUCCESS;
import static com.tcs.inventoryms.constants.MessageConstants.SAVE_SUCCESS;
import static com.tcs.inventoryms.constants.MessageConstants.UPDATE_SUCCESS;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.inventoryms.entities.BusInventory;
import com.tcs.inventoryms.exceptions.BusInventoryAlreadyExistsException;
import com.tcs.inventoryms.exceptions.BusInventoryNotFoundException;
import com.tcs.inventoryms.exceptions.InvalidNoOfSeatsException;
import com.tcs.inventoryms.repository.BusInventoryRepository;
import com.tcs.inventoryms.service.InventoryService;
import com.tcs.inventoryms.vo.BookingVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BusInventoryController {
	
	private final BusInventoryRepository busInventoryRepository;
	
	private final InventoryService inventoryService;

	
	@GetMapping("/busInventory/all")
	public List<BusInventory> getAllBusInventories() {
		return busInventoryRepository.findAll();
	}
	
	@GetMapping("/busInventory/{busNumber}")
	public ResponseEntity<BusInventory> getBusInventoryByBusNumber(@PathVariable String busNumber) {
		BusInventory inventory = busInventoryRepository.findById(busNumber)
				.orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		return ResponseEntity.ok().body(inventory);
	}
	
	@GetMapping("/busInventory/seats/{busNumber}")
	public ResponseEntity<Integer> getAvailableSeatsByBusNumber(@PathVariable String busNumber) {
		BusInventory inventory = busInventoryRepository.findById(busNumber)
				.orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		return ResponseEntity.ok().body(inventory.getAvailableSeats());
	}
	
	@PostMapping("/busInventory")
	public ResponseEntity<?> saveBusInventory(@RequestBody BusInventory busInventory) {
		BusInventory inventory = busInventoryRepository.findById(busInventory.getBusNumber()).orElse(null);
		if (inventory == null) {
			busInventory.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
			busInventoryRepository.save(busInventory);
			return new ResponseEntity<>(SAVE_SUCCESS + busInventory.getBusNumber(), HttpStatus.OK);
		} else {
			throw new BusInventoryAlreadyExistsException(ERR_MSG_ALREADY_EXISTS + busInventory.getBusNumber());
		}
	}
	
	@PutMapping("/busInventory/{busNumber}")
	public ResponseEntity<?> updateBusInventoryByBusNumber(@PathVariable String busNumber, @RequestBody BusInventory inventory) {
		BusInventory existingInventory = busInventoryRepository.findById(busNumber)
                .orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		existingInventory.setAvailableSeats(inventory.getAvailableSeats());
		existingInventory.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
		busInventoryRepository.save(existingInventory);
		return new ResponseEntity<>(UPDATE_SUCCESS + inventory.getBusNumber(), HttpStatus.OK);
	}
	
	@DeleteMapping("/busInventory/{busNumber}")
	public ResponseEntity<?> deleteBusInventoryByBusNumber(@PathVariable String busNumber) {
		busInventoryRepository.findById(busNumber)
                .orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		busInventoryRepository.deleteById(busNumber);
		return new ResponseEntity<>(DELETE_SUCCESS + busNumber, HttpStatus.OK);
	}
	
	@PutMapping("/busInventory/{busNumber}/add/{noOfSeats}")
	public ResponseEntity<?> addToBusInventoryByBusNumber(@PathVariable String busNumber, @PathVariable Integer noOfSeats) {
		if (noOfSeats == null) {
			new InvalidNoOfSeatsException(ERR_INVALID_NO_OF_SEATS);
		}
		BusInventory existingInventory = busInventoryRepository.findById(busNumber)
                .orElseThrow(()->new BusInventoryNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		existingInventory.setAvailableSeats(existingInventory.getAvailableSeats() + noOfSeats);
		existingInventory.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
		busInventoryRepository.save(existingInventory);
		return new ResponseEntity<>(UPDATE_SUCCESS + busNumber, HttpStatus.OK);
	}
	
	@PostMapping("/busInventory/reduce")
	public ResponseEntity<?> reducefromBusInventoryByBusNumber(@RequestBody BookingVo bookingVo) {
		inventoryService.reducefromBusInventoryByBusNumber(bookingVo);
		return new ResponseEntity<>(UPDATE_SUCCESS + bookingVo.getBusNumber(), HttpStatus.OK);
	}

}
