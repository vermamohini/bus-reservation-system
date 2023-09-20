package com.tcs.inventoryms.exceptions;

//custom exception that can be thrown when Available number of seats less than required number of seats in the inventory
public class InventoryException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
    
    public InventoryException(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
