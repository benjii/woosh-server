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
	
//	private String base64BinaryValue = null;

	
	public CardDataBean() {
		// default constructor required by Spring MVC
	}

	public CardDataBean(String name, String value, CardDataType type) {
		this.name = name;
		this.type = type;
		this.value = value;
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

//	public String getBase64BinaryValue() {
//		return base64BinaryValue;
//	}
//
//	public void setBase64BinaryValue(String base64BinaryValue) {
//		this.base64BinaryValue = base64BinaryValue;
//	}

}
