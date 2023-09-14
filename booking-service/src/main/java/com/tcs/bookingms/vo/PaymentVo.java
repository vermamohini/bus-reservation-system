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
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = PaymentVo.class)
public class PaymentVo implements Serializable {
	
	private Integer bookingNumber;
	private Double amount;
	
	public PaymentVo(Integer bookingNumber, Double amount) {
		super();
		this.bookingNumber = bookingNumber;
		this.amount = amount;
	}

	public PaymentVo() {
		super();
	}

	@Override
	public String toString() {
		return "PaymentVo [bookingNumber=" + bookingNumber + ", amount=" + amount + "]";
	}
	
}
