package com.luminos.woosh.domain.common;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * 
 * @author Ben
 */
@Entity
public class Configuration {

	@Id
	@GeneratedValue(generator="default")
	@GenericGenerator(name="default", strategy="uuid", parameters={@Parameter(name="separator",value="-")})
	private String id = null;

	@Version
	private Integer version = null;

	private String key = null;
	
	private String value = null;

	
	public Configuration() {

	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
