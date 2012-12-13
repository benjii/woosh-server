package com.luminos.woosh.beans;

import com.luminos.woosh.enums.CardDataType;

/**
 * 
 * @author Ben
 */
public class CardDataBean {

	private String name = null;

	private CardDataType type = null;
	
	private String data = null;

	
	public CardDataBean(String name, String data, CardDataType type) {
		this.name = name;
		this.type = type;
		this.data = data;
	}

	
	public String getName() {
		return name;
	}

	public String getData() {
		return data;
	}

	public CardDataType getType() {
		return type;
	}
	
}
