package com.luminos.woosh.beans;

import com.luminos.woosh.enums.CardDataType;

/**
 * 
 * @author Ben
 */
public class CardDataBean {

	private String name = null;

	private CardDataType type = null;
	
	// if type == TEXT then this is a literal value
	// if type == BINARY then this is a base64 encoded value
	private String value = null;
	
	// if the data is binary then it will have a UUID
	private String binaryId = null;
	
	
	public CardDataBean() {
		// default constructor required by Spring MVC
	}

	public CardDataBean(String name, String value, CardDataType type) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public CardDataBean(String name, String value, String binaryId) {
		this(name, value, CardDataType.BINARY);
		this.binaryId = binaryId;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CardDataType getType() {
		return type;
	}

	public void setType(CardDataType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getBinaryId() {
		return binaryId;
	}

	public void setBinaryId(String binaryId) {
		this.binaryId = binaryId;
	}

}
