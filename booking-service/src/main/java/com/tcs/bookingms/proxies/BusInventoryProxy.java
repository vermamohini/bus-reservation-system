package com.tcs.bookingms.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service")
public interface BusInventoryProxy {
	
	@GetMapping("/api/v1/busInventory/seats/{busNumber}")
	public ResponseEntity<Integer> getAvailableSeatsByBusNumber(@PathVariable("busNumber") String busNumber);

}
