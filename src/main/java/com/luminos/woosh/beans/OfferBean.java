package com.luminos.woosh.beans;

import java.sql.Timestamp;

/**
 * 
 * @author Ben
 */
public class OfferBean {

	private String id = null;
	
	private String cardId = null;
	
	private Timestamp offerStart = null;

	private Timestamp offerEnd = null;
	
	private Double latitude = null;

	private Double longitude = null;
	
	private Boolean autoAccept = null;

	
	public OfferBean() {
		
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public Timestamp getOfferStart() {
		return offerStart;
	}

	public void setOfferStart(Timestamp offerStart) {
		this.offerStart = offerStart;
	}

	public Timestamp getOfferEnd() {
		return offerEnd;
	}

	public void setOfferEnd(Timestamp offerEnd) {
		this.offerEnd = offerEnd;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Boolean getAutoAccept() {
		return autoAccept;
	}

	public void setAutoAccept(Boolean autoAccept) {
		this.autoAccept = autoAccept;
	}
	
}
