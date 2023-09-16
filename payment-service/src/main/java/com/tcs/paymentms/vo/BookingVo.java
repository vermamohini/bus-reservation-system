package com.tcs.paymentms.vo;

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
	
	private Integer bookingNumber;
	private Double amount;
	private Integer noOfSeats;
	
	public BookingVo(Integer bookingNumber, Double amount, Integer noOfSeats) {
		super();
		this.bookingNumber = bookingNumber;
		this.amount = amount;
		this.noOfSeats = noOfSeats;
	}

	public BookingVo() {
		super();
	}

	@Override
	public String toString() {
		return "PaymentVo [bookingNumber=" + bookingNumber + ", amount=" + amount + ", noOfSeats=" + noOfSeats + "]";
	}
	
}
