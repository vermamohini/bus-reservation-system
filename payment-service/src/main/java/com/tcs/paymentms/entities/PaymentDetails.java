package com.tcs.paymentms.entities;

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
public class PaymentDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer paymentNumber;
	
	private Integer bookingNumber;
	
	private Double amount;
	
	private Timestamp paymentDate;
	
	

}
