package com.tcs.bookingms.vo;

import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullBookingDetailsVo {
	
	private Integer bookingNumber;
	
	private String busNumber;
	
	private String startPoint;
	
	private String endPoint;
	
	private Integer noOfSeats;
	
	private Timestamp bookingDate;
	
	private String bookingStatus;
	
	private List<Integer> passengerIds;
	
}
