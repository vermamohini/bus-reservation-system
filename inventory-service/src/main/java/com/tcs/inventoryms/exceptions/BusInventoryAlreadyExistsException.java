package com.tcs.inventoryms.exceptions;

//custom exception that can be thrown when user tries to add a bus inventory that already exists
public class BusInventoryAlreadyExistsException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public BusInventoryAlreadyExistsException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
