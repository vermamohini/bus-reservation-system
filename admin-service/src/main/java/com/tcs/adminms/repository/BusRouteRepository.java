package com.tcs.adminms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.adminms.entities.BusRoute;

public interface BusRouteRepository extends JpaRepository<BusRoute, String> {
	
	public List<BusRoute> findByStartPointAndEndPoint(String startPoint, String endPoint);

}
