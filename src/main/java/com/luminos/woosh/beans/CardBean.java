package com.luminos.woosh.beans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.Offer;

/**
 * 
 * @author Ben
 */
public class CardBean {

	private String id = null;

	private String name = null;

	private Timestamp lastOfferStart = null;

	private Timestamp lastOfferEnd = null;

	private String lastOfferId = null;
	
	private List<CardDataBean> data = null;

	
	public CardBean() {
		// default constructor required by Spring MVC
	}

	public CardBean(Card card) {
		this.id = card.getClientId();
		this.name = card.getName();
		if (card.getLastOffer() != null) {
			this.lastOfferStart = card.getLastOffer().getOfferStart();
			this.lastOfferEnd = card.getLastOffer().getOfferEnd();
			this.lastOfferId = card.getLastOffer().getClientId();
		}
	}
	
	public CardBean(String id, String name, Offer lastOffer) {
		this.id = id;
		this.name = name;
		if (lastOffer != null) {
			this.lastOfferStart = lastOffer.getOfferStart();
			this.lastOfferEnd = lastOffer.getOfferEnd();
			this.lastOfferId = lastOffer.getClientId();
		}
	}


	public void addDatum(CardDataBean dataBean) {
		if (this.data == null) {
			this.data = new ArrayList<CardDataBean>();
		}
		this.data.add(dataBean);
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getLastOfferStart() {
		return lastOfferStart;
	}

	public void setLastOfferStart(Timestamp lastOfferStart) {
		this.lastOfferStart = lastOfferStart;
	}

	public Timestamp getLastOfferEnd() {
		return lastOfferEnd;
	}

	public void setLastOfferEnd(Timestamp lastOfferEnd) {
		this.lastOfferEnd = lastOfferEnd;
	}

	public String getLastOfferId() {
		return lastOfferId;
	}

	public void setLastOfferId(String lastOfferId) {
		this.lastOfferId = lastOfferId;
	}

	public List<CardDataBean> getData() {
		return data;
	}

	public void setData(List<CardDataBean> data) {
		this.data = data;
	}
	
}
