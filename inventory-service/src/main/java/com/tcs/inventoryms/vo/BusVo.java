package com.tcs.inventoryms.vo;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = BusVo.class)
public class BusVo implements Serializable {
	
	private String busNumber;
	private Integer noOfSeats;
	
	public BusVo(String busNumber, Integer noOfSeats) {
		super();
		this.busNumber = busNumber;
		this.noOfSeats = noOfSeats;
	}

	public BusVo() {
		super();
	}

	@Override
	public String toString() {
		return "BusVo [busNumber=" + busNumber + ", noOfSeats=" + noOfSeats + "]";
	}

}
