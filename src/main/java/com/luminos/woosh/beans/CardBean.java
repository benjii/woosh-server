package com.luminos.woosh.beans;

import java.util.ArrayList;
import java.util.List;

import com.luminos.woosh.domain.Card;

/**
 * 
 * @author Ben
 */
public class CardBean {

	private String id = null;

	private String name = null;

	// TODO thinking about including this, but leaving it out for now
//	private String lastOfferStart = null;
//
//	private String lastOfferEnd = null;

	private List<CardDataBean> data = null;

	
	public CardBean() {
		// default constructor required by Spring MVC
	}

	public CardBean(Card card) {
		this.id = card.getClientId();
		this.name = card.getName();
	}
	
	public CardBean(String id, String name) {
		this.id = id;
		this.name = name;
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

	public List<CardDataBean> getData() {
		return data;
	}

	public void setData(List<CardDataBean> data) {
		this.data = data;
	}
	
}
