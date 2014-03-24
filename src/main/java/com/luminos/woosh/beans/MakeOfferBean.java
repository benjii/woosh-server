package com.luminos.woosh.beans;


/**
 * 
 * @author Ben
 */
public class MakeOfferBean {

	private String cardId = null;
	
	private Integer duration = null;

	private Double latitude = null;

	private Double longitude = null;

	private Boolean autoAccept = null;

	
	public MakeOfferBean() {
		
	}


	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
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
