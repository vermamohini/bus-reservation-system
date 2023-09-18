package com.tcs.inventoryms.entities;

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
public class InventoryUpdateLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer logId;
	
	private Integer bookingNumber;
	
	private Integer noOfSeats;
	
	private String operation;
	
	private Timestamp createdDate;
	
}
