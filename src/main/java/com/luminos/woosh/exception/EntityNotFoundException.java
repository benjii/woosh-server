package com.luminos.woosh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * @author Ben
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4098450705342331610L;

	private String id = null;
	
	
	public EntityNotFoundException() {
		
	}

	public EntityNotFoundException(String id, String message) {
		super(message);
		this.id = id;
	}

	
	public String getId() {
		return id;
	}
	
}
