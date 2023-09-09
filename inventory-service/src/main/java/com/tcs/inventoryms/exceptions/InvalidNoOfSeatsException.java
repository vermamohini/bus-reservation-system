package com.tcs.inventoryms.exceptions;

//custom exception that can be thrown when user tries to update/delete a bus inventory with invalid number of seats
public class InvalidNoOfSeatsException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public InvalidNoOfSeatsException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
