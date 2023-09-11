package com.tcs.paymentms.controller;

import static com.tcs.paymentms.constants.ErrorConstants.ERR_MSG_ALREADY_EXISTS;
import static com.tcs.paymentms.constants.MessageConstants.SAVE_SUCCESS;

import java.sql.Timestamp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.paymentms.entities.PaymentDetails;
import com.tcs.paymentms.exceptions.PaymentAlreadyExistsException;
import com.tcs.paymentms.repository.PaymentDetailsRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentDetailsRepository paymentDetailsRepository;
	
	@PostMapping("/payment")
	public ResponseEntity<?> savePayment(@RequestBody PaymentDetails paymentDetails) {
		PaymentDetails existingPayment = paymentDetailsRepository.findByBookingNumber(paymentDetails.getBookingNumber());
		if (existingPayment == null) {
			paymentDetails.setPaymentDate(new Timestamp(System.currentTimeMillis()));
			paymentDetailsRepository.save(paymentDetails);
			return new ResponseEntity<>(SAVE_SUCCESS + paymentDetails.getBookingNumber(), HttpStatus.OK);
		} else {
			throw new PaymentAlreadyExistsException(ERR_MSG_ALREADY_EXISTS + paymentDetails.getBookingNumber());
		}
	}

}
