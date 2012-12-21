package com.luminos.woosh.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.luminos.woosh.base.AbstractLuminosIntegrationTest;
import com.luminos.woosh.beans.OfferBean;
import com.luminos.woosh.beans.Receipt;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.util.GeoSpatialUtils;

/**
 * These are tests for the Woosh controller API (direct calls to the controller itself).
 * 
 * TODO create a separate set of tests for exercising the REST methods (via URIs).
 * 
 * @author Ben
 */
public class WooshControllerIntTest extends AbstractLuminosIntegrationTest {
	
	@Autowired
	private WooshController wooshController = null;
	
	@Autowired
	private CardDao cardDao = null; 

	@Autowired
	private OfferDao offerDao = null; 

	
	@Test
	public void canPostNewCard() {
	
		// post a new card through the Woosh API
		Receipt r = wooshController.addCard("test");
		
		// ensure that the new entity was flushed to the database
		assertEquals(1, super.countRowsInTable("Card"));
		
		// ensure that the card is correct in the database
		Card postedCard = cardDao.findByClientId(r.getId());
		assertEquals("Card name was not as expected.", "test", postedCard.getName());
	}

	@Test
	public void canPostNewOffer() {

		// create a card that can be offered
		Card card = new Card(END_USER, "Test");
		cardDao.save(card);
		
		// make an offer on the card
		wooshController.makeOffer(card.getClientId(), 3000, 0.15D, 122.2D, Boolean.TRUE);

		// ensure that it all ended up in the database
		assertEquals(1, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("Offer"));
		
	}

	@Test
	public void canFindOfffers() {

		// note that we use the user 'OFFERING_USER' here instead of the default (pre-authenticated 'END_USER')
		// this is because user's aren't able to pick up their own offers, so we need to scan with someone other
		// than the user that is 'logged in'.
		
		// create a card that can be offered
		Card card = new Card(OFFERING_USER, "Test");
		cardDao.save(card);

		// offer the card
		Offer offer = new Offer(OFFERING_USER, card, 6000, GeoSpatialUtils.createPoint(0.15D, 122.2D), Boolean.TRUE);
		offer.setAutoAccept(Boolean.TRUE);
		offerDao.save(offer);

		// ensure that it all ended up in the database
		assertEquals(1, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("Offer"));

		// now scan for the offer
		List<OfferBean> availableOffers = wooshController.findOffers(0.15D, 122.2D);
		
		assertEquals(1, super.countRowsInTable("Scan"));
		assertEquals(1, super.countRowsInTable("Acceptance"));
		assertEquals(2, super.countRowsInTable("Card"));
		
		assertEquals(1, availableOffers.size());
		
	}

}
