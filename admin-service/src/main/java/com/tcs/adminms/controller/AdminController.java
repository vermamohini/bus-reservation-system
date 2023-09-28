package com.tcs.adminms.controller;

import static com.tcs.adminms.constants.ErrorConstants.ERR_MSG_ALREADY_EXISTS;
import static com.tcs.adminms.constants.ErrorConstants.ERR_MSG_NOT_FOUND;
import static com.tcs.adminms.constants.MessageConstants.DELETE_SUCCESS;
import static com.tcs.adminms.constants.MessageConstants.SAVE_SUCCESS;
import static com.tcs.adminms.constants.MessageConstants.UPDATE_SUCCESS;

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

import com.tcs.adminms.entities.BusRoute;
import com.tcs.adminms.exceptions.BusRouteAlreadyExistsException;
import com.tcs.adminms.exceptions.BusRouteNotFoundException;
import com.tcs.adminms.repository.BusRouteRepository;
import com.tcs.adminms.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AdminController {
	
	private final BusRouteRepository busRouteRepository;
	
	private final AdminService adminService;
	
	@GetMapping("/busRoutes")
	public List<BusRoute> getAllBusRoutes() {
		return busRouteRepository.findAll();
	}
	
	@GetMapping("/busRoutes/{busNumber}")
	public ResponseEntity<BusRoute> getBusRouteByBusNumber(@PathVariable String busNumber) {
		BusRoute route = busRouteRepository.findById(busNumber)
                .orElseThrow(()->new BusRouteNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		return ResponseEntity.ok().body(route);
	}
	
	@PostMapping("/busRoutes")
	public ResponseEntity<?> saveBusRoute(@RequestBody BusRoute route) {
		adminService.saveBusRoute(route);
		return new ResponseEntity<>(SAVE_SUCCESS + route.getBusNumber(), HttpStatus.OK);	
	}
	
	@PutMapping("/busRoutes/{busNumber}")
	public ResponseEntity<?> updateBusRouteByBusNumber(@PathVariable String busNumber, @RequestBody BusRoute route) {
		BusRoute existingRoute = busRouteRepository.findById(busNumber)
                .orElseThrow(()->new BusRouteNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		existingRoute.setStartPoint(route.getStartPoint());
		existingRoute.setEndPoint(route.getEndPoint());
		existingRoute.setPricePerTicket(route.getPricePerTicket());
		existingRoute.setTotalSeats(route.getTotalSeats());
		busRouteRepository.save(existingRoute);
		return new ResponseEntity<>(UPDATE_SUCCESS + route.getBusNumber(), HttpStatus.OK);
	}
	
	@DeleteMapping("/busRoutes/{busNumber}")
	public ResponseEntity<?> deleteBusRouteByBusNumber(@PathVariable String busNumber) {
		busRouteRepository.findById(busNumber)
                .orElseThrow(()->new BusRouteNotFoundException(ERR_MSG_NOT_FOUND + busNumber));
		busRouteRepository.deleteById(busNumber);
		return new ResponseEntity<>(DELETE_SUCCESS + busNumber, HttpStatus.OK);
	}
	
	@GetMapping("/busRoutes/start/{startPoint}/end/{endPoint}")
	public List<BusRoute> getBusRoutesBetween(@PathVariable String startPoint, @PathVariable String endPoint) {
		return busRouteRepository.findByStartPointAndEndPoint(startPoint, endPoint);
	}

}
