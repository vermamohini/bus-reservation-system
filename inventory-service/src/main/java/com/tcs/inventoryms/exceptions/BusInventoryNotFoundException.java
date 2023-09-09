package com.tcs.inventoryms.exceptions;

//custom exception that can be thrown when user tries to update/delete a bus inventory that doesn't exists
public class BusInventoryNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public BusInventoryNotFoundException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
