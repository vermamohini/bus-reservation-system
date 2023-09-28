package com.tcs.paymentms.controller;

import static com.tcs.paymentms.constants.MessageConstants.REFUND_SUCCESS;
import static com.tcs.paymentms.constants.MessageConstants.SAVE_SUCCESS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.paymentms.service.PaymentService;
import com.tcs.paymentms.vo.BookingVo;
import com.tcs.paymentms.vo.FullPaymentDetailsVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;

	
	@PostMapping("/savePayment")
	public ResponseEntity<String> savePayment(@RequestBody BookingVo bookingDetails) {
		paymentService.savePayment(bookingDetails);
		return new ResponseEntity<>(SAVE_SUCCESS + bookingDetails.getBookingNumber(), HttpStatus.OK);
	}
	
	@PostMapping("/refundPayment")
	public ResponseEntity<String> refundPayment(@RequestBody BookingVo bookingDetails) {
		paymentService.refundPayment(bookingDetails);
		return new ResponseEntity<>(REFUND_SUCCESS + bookingDetails.getBookingNumber(), HttpStatus.OK);
	}
	
	@GetMapping("/getPayment/paymentNumber/{paymentNumber}")
	public FullPaymentDetailsVo getPaymentByPaymentNumber(@PathVariable Integer paymentNumber) {
		return paymentService.getPaymentByPaymentNumber(paymentNumber);
	}
	
	@GetMapping("/getPayment/bookingNumber/{bookingNumber}")
	public FullPaymentDetailsVo getPaymentByBookingNumber(@PathVariable Integer bookingNumber) {
		return paymentService.getPaymentByBookingNumber(bookingNumber);
	}

}
