package com.tcs.paymentms.controller;

import static com.tcs.paymentms.constants.MessageConstants.SAVE_SUCCESS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.paymentms.service.PaymentService;
import com.tcs.paymentms.vo.BookingVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;

	
	@PostMapping("/payment")
	public ResponseEntity<String> savePayment(@RequestBody BookingVo bookingDetails) {
		paymentService.savePayment(bookingDetails);
		return new ResponseEntity<>(SAVE_SUCCESS + bookingDetails.getBookingNumber(), HttpStatus.OK);
	}

}
