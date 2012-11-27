package com.luminos.woosh.synchronization.service;

/**
 * 
 * @author Ben
 */
public class FieldDefinition {

	private String name = null;
	
	private String type = null;

	
	public FieldDefinition(String name, String type) {
		this.name = name;
		this.type = type;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}	
	
}
