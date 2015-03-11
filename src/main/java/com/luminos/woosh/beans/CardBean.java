package com.luminos.woosh.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ben
 */
public class CardBean {

	private String id = null;

	private OfferBean lastOffer = null;
	
	private OfferBean fromOffer = null;

	private Integer totalOffers = null;
	
	private Integer totalAcceptances = null;
	
	private List<CardDataBean> data = null;

	
	public CardBean() {
		// default constructor required by Spring MVC
	}
	
	public CardBean(String id) {
		this.id = id;
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

	public Integer getTotalOffers() {
		return totalOffers;
	}

	public void setTotalOffers(Integer totalOffers) {
		this.totalOffers = totalOffers;
	}

	public Integer getTotalAcceptances() {
		return totalAcceptances;
	}

	public void setTotalAcceptances(Integer totalAcceptances) {
		this.totalAcceptances = totalAcceptances;
	}

	public List<CardDataBean> getData() {
		return data;
	}

	public void setData(List<CardDataBean> data) {
		this.data = data;
	}
	
}
