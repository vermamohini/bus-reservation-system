package com.tcs.paymentms.exceptions;

//custom exception that can be thrown when user tries to add payment with invalid amount
public class InvalidAmountException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public InvalidAmountException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
