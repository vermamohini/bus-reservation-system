package com.tcs.adminms.exceptions;

//custom exception that can be thrown when user tries to update/delete a bus route that doesn't exists
public class BusRouteNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public BusRouteNotFoundException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
