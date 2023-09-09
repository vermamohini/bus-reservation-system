package com.tcs.adminms.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(value
			= BusRouteNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody ErrorResponse
	handleException(BusRouteNotFoundException ex)
	{
		return new ErrorResponse(
				HttpStatus.NOT_FOUND.value(), ex.getMessage());
	}
	
	@ExceptionHandler(value
			= BusRouteAlreadyExistsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody ErrorResponse
	handleException(BusRouteAlreadyExistsException ex)
	{
		return new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}

}
