package com.luminos.woosh.synchronization;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Receipts are sent back to the client for posted (new) objects and updated ones. Only when the client 
 * device receives and processes the receipt that it will know that the (atomic) update has completed.
 * 
 * @author Ben
 */
public class Receipt {

	private String clientId = null;
	
	private Integer clientVersion = null;
	
	private Timestamp lastUpdated = null;
	
	private Map<String, String> additionalProperties = null;

		
	public Receipt(String clientId, Integer clientVersion, Timestamp lastUpdated) {
		this.clientId = clientId;
		this.clientVersion = clientVersion;
		this.lastUpdated = lastUpdated;
	}

	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Integer getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(Integer clientVersion) {
		this.clientVersion = clientVersion;
	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Map<String, String> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, String> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
	
}
