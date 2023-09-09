package com.tcs.adminms.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BusRoute {
	
	@Id
	private String busNumber;
	
	private String startPoint;
	
	private String endPoint;
	
	private Double pricePerTicket;
	
	private Integer totalSeats;
	
}
