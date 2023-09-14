package com.tcs.bookingms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.bookingms.entities.BusRoute;

public interface BusRouteRepository extends JpaRepository<BusRoute, String> {
	
	

}
