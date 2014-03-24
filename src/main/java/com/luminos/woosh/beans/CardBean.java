package com.luminos.woosh.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ben
 */
public class CardBean {

	private String id = null;

	private String name = null;

	private OfferBean lastOffer = null;
	
	private OfferBean fromOffer = null;
	
//	private Timestamp lastOfferStart = null;
//
//	private Timestamp lastOfferEnd = null;
//
//	private String lastOfferId = null;
//
//	private Timestamp fromOfferStart = null;
//
//	private Timestamp fromOfferEnd = null;
//
//	private String fromOfferId = null;

	private List<CardDataBean> data = null;

	
	public CardBean() {
		// default constructor required by Spring MVC
	}

//	public CardBean(Card card) {
//		this.id = card.getClientId();
//		this.name = card.getName();
////		if (card.getLastOffer() != null) {
////			this.lastOfferStart = card.getLastOffer().getOfferStart();
////			this.lastOfferEnd = card.getLastOffer().getOfferEnd();
////			this.lastOfferId = card.getLastOffer().getClientId();
////		}
////		if (card.getFromOffer() != null) {
////			this.fromOfferStart = card.getFromOffer().getOfferStart();
////			this.fromOfferEnd = card.getFromOffer().getOfferEnd();
////			this.fromOfferId = card.getFromOffer().getClientId();
////		}
//	}
	
	public CardBean(String id, String name /*, Offer lastOffer, Offer fromOffer */) {
		this.id = id;
		this.name = name;
//		if (lastOffer != null) {
//			this.lastOfferStart = lastOffer.getOfferStart();
//			this.lastOfferEnd = lastOffer.getOfferEnd();
//			this.lastOfferId = lastOffer.getClientId();
//		}
//		if (fromOffer != null) {
//			this.fromOfferStart = fromOffer.getOfferStart();
//			this.fromOfferEnd = fromOffer.getOfferEnd();
//			this.fromOfferId = fromOffer.getClientId();
//		}
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

	public OfferBean getLastOffer() {
		return lastOffer;
	}

	public void setLastOffer(OfferBean lastOffer) {
		this.lastOffer = lastOffer;
	}

	public OfferBean getFromOffer() {
		return fromOffer;
	}

	public void setFromOffer(OfferBean fromOffer) {
		this.fromOffer = fromOffer;
	}

	public List<CardDataBean> getData() {
		return data;
	}

	public void setData(List<CardDataBean> data) {
		this.data = data;
	}
	
}
