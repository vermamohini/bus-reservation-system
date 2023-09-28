package com.tcs.paymentms.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullPaymentDetailsVo {
	
	private Integer paymentNumber;
	
	private Integer bookingNumber;
	
	private Double amount;
	
	private Timestamp paymentDate;
	
	private String paymentStatus;
	
}
