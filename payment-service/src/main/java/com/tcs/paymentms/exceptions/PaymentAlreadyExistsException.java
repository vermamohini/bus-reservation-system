package com.tcs.paymentms.exceptions;

//custom exception that can be thrown when user tries to add a payment for a booking that already exists
public class PaymentAlreadyExistsException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public PaymentAlreadyExistsException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
