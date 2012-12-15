package com.luminos.woosh.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.beans.CardDataBean;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.enums.CardDataType;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;

/**
 * 
 * @author Ben
 */
@Service
public class BeanConverterService {

	@Autowired
	private CloudServiceProxy s3proxy = null;


	/**
	 * Converts a single card to its bean representation.
	 * 
	 * @param card
	 * @return
	 */
	public CardBean convertCard(Card card) {
		if (card == null) return null;
		
		CardBean cardBean = new CardBean(card);
		
		if (card.getData() != null) {
			for (CardData data : card.getData()) {
				cardBean.addDatum(this.convertCardDataToBean(data));
			}			
		}
		
		return cardBean;		
	}

	/**
	 * Converts a list of cards to the bean representations.
	 * 
	 * @param cards
	 * @return
	 */
	public List<CardBean> convertCards(List<Card> cards) {
		List<CardBean> beans = new ArrayList<CardBean>();
		
		for (Card card : cards) {
			CardBean cardBean = new CardBean(card.getClientId(), card.getName());
			
			if (card.getData() != null) {
				for (CardData data : card.getData()) {
					cardBean.addDatum(this.convertCardDataToBean(data));
				}
			}
			
			beans.add(cardBean);
		}
		
		return beans;		
	}
	
	
	private CardDataBean convertCardDataToBean(CardData data) {
		CardDataBean dataBean = null;
		
		if ( StringUtils.isNotBlank(data.getData()) ) {
			dataBean = new CardDataBean(data.getName(), data.getData(), CardDataType.TEXT);
		} else {
			dataBean = new CardDataBean(data.getName(), s3proxy.createSignedUrl(data.getBinaryData()), CardDataType.BINARY);					
		}

		return dataBean;
	}
	
	
}
