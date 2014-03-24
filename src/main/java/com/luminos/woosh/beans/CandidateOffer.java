package com.luminos.woosh.beans;


/**
 * 
 * @author Ben
 */
public class CandidateOffer {

	private String offerId = null;

	private CardBean offeredCard = null;
		
		
	public CandidateOffer(String offerId, CardBean offeredCard) {
		this.offerId = offerId;
		this.offeredCard = offeredCard;
	}


	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public CardBean getOfferedCard() {
		return offeredCard;
	}

	public void setOfferedCard(CardBean offeredCard) {
		this.offeredCard = offeredCard;
	}
	
}
