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
public class PaymentStatus {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer statusId;
	
	private Integer paymentNumber;
	
	private String paymentStatus;
	
	private Timestamp createdDate;
	
}
