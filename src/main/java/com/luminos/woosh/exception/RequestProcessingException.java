package com.luminos.woosh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * @author Ben
 */
@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class RequestProcessingException extends RuntimeException {

	private static final long serialVersionUID = 4779696316268943762L;


	public RequestProcessingException() {
		super();
	}

	public RequestProcessingException(String message) {
		super(message);
	}
	
}
