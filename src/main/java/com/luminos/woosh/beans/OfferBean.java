package com.luminos.woosh.beans;

/**
 * 
 * @author Ben
 */
public class OfferBean {

	private String id = null;

	private CardBean offeredCard = null;

		
	public OfferBean(String id, CardBean offeredCard) {
		this.id = id;
		this.offeredCard = offeredCard;
	}

	
	public String getId() {
		return id;
	}

	public CardBean getOfferedCard() {
		return offeredCard;
	}
	
	
	
}
