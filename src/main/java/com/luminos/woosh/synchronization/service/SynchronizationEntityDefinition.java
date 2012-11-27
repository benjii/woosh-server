package com.luminos.woosh.synchronization.service;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Ben
 */
public class SynchronizationEntityDefinition {

	private Set<FieldDefinition> fields = null;

	
	public SynchronizationEntityDefinition() {
		this.fields = new HashSet<FieldDefinition>();
	}

	
	/**
	 * Adds a field definition to the list.
	 * 
	 * @param def
	 */
	public void addFieldDefinition(FieldDefinition def) {
		if (def != null) {
			this.fields.add(def);
		}
	}
	
	/**
	 * Adds a field definition to the list.
	 * 
	 * @param name
	 * @param type
	 */
	public void addFieldDefinition(String name, String type) {
		if (name != null && type != null) {
			this.fields.add(new FieldDefinition(name, type));
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getFieldCount() {
		return fields.size();
	}
	
	
	public Set<FieldDefinition> getFields() {
		return fields;
	}

	public void setFields(Set<FieldDefinition> fields) {
		this.fields = fields;
	}
	
}