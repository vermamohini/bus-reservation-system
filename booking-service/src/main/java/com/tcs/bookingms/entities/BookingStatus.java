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
public class BookingStatus {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer statusId;
	
	private String bookingNumber;
	
	private String bookingStatus;
	
	private Timestamp createdDate;
	
}
