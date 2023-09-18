package com.tcs.bookingms.vo;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = BookingVo.class)
public class BookingVo implements Serializable {
	
	private String busNumber;
	private Integer bookingNumber;
	private Double amount;
	private Integer noOfSeats;
	
	public BookingVo(String busNumber, Integer bookingNumber, Double amount, Integer noOfSeats) {
		super();
		this.busNumber = busNumber;
		this.bookingNumber = bookingNumber;
		this.amount = amount;
		this.noOfSeats = noOfSeats;
	}

	public BookingVo() {
		super();
	}

	@Override
	public String toString() {
		return "BookingVo [busNumber=" + busNumber + ", bookingNumber=" + bookingNumber + ", amount=" + amount + ", noOfSeats=" + noOfSeats + "]";
	}
	
}
