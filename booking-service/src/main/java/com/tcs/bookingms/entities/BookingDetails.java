package com.tcs.bookingms.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BookingDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer bookingNumber;
	
	private String busNumber;
	
	private String startPoint;
	
	private String endPoint;
	
	private Integer noOfSeats;
	
	private Timestamp bookingDate;
	
}
