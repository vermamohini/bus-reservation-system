package com.tcs.inventoryms.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BusInventory {
	
	@Id
	private String busNumber;
	
	private Integer availableSeats;
	
	private Timestamp lastUpdatedDate;
	
	

}
