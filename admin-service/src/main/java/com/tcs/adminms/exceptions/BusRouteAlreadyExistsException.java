package com.tcs.adminms.exceptions;

//custom exception that can be thrown when user tries to add a bus route that already exists
public class BusRouteAlreadyExistsException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public BusRouteAlreadyExistsException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
