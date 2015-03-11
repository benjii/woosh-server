package com.luminos.woosh.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.beans.CardDataBean;
import com.luminos.woosh.beans.OfferBean;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.enums.CardDataType;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;
import com.vividsolutions.jts.geom.Point;

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
		
		CardBean cardBean = new CardBean(card.getClientId() /*, card.getName() */);
		
		// create beans for all of the data objects
		if (card.getData() != null) {
			for (CardData data : card.getData()) {
				cardBean.addDatum(this.convertCardDataToBean(data));
			}			
		}
		
		// create offer beans
		if (card.getLastOffer() != null) {
			cardBean.setLastOffer(this.convertOffer(card.getLastOffer()));			
		}
		if (card.getFromOffer() != null) {
			cardBean.setFromOffer(this.convertOffer(card.getFromOffer()));			
		}
		if ( card.getOffers() != null ) {
			// TODO this may not be optimal...
			//		( (Integer) s.createFilter( collection, "select count(*)" ).list().get(0) ).intValue()
			cardBean.setTotalOffers(card.getOffers().size());			
		}
		if ( card.getAcceptances() != null ) {
			// TODO this may not be optimal...
			//		( (Integer) s.createFilter( collection, "select count(*)" ).list().get(0) ).intValue()
			cardBean.setTotalAcceptances(card.getAcceptances().size());			
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
			CardBean cardBean = this.convertCard(card); //new CardBean(card.getClientId(), card.getName()/*, card.getLastOffer(), card.getFromOffer()*/);
						
			beans.add(cardBean);
		}
		
		return beans;		
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private CardDataBean convertCardDataToBean(CardData data) {
		CardDataBean dataBean = null;
		
		if ( StringUtils.isNotBlank(data.getData()) ) {
			dataBean = new CardDataBean(data.getName(), data.getData(), CardDataType.TEXT);
		} else {
			dataBean = new CardDataBean(data.getName(), s3proxy.createSignedUrl(data.getBinaryData()), data.getBinaryData().getRemoteId());
		}

		return dataBean;
	}
	
	/**
	 * 
	 * @param offer
	 * @return
	 */
	private OfferBean convertOffer(Offer offer) {
		OfferBean offerBean = new OfferBean();
		
		offerBean.setId(offer.getClientId());
		offerBean.setCardId(offer.getCard().getClientId());
		offerBean.setLatitude(((Point) offer.getOfferRegion()).getX());
		offerBean.setLongitude(((Point) offer.getOfferRegion()).getY());
		offerBean.setOfferStart(offer.getOfferStart());
		offerBean.setOfferEnd(offer.getOfferEnd());
		offerBean.setAutoAccept(offer.getAutoAccept());
		
		return offerBean;
	}	
	
}
