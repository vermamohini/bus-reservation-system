package com.tcs.bookingms.exceptions;

//custom exception that can be thrown when user tries to update/delete a booking that doesn't exists
public class BookingNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public BookingNotFoundException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
