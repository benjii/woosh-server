package com.luminos.woosh.controller;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.luminos.woosh.base.AbstractLuminosIntegrationTest;
import com.luminos.woosh.beans.CandidateOffer;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.domain.Acceptance;
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
	
	@Autowired
	private AcceptanceDao acceptanceDao = null;


	@Test
	public void canFindOffers() {

		// note that we use the user 'OFFERING_USER' here instead of the default (pre-authenticated 'END_USER')
		// this is because user's aren't able to pick up their own offers, so we need to scan with someone other
		// than the user that is 'logged in'.
		
		// create a card that can be offered
		Card card = new Card(OFFERING_USER /*, "Test" */);
		cardDao.save(card);

		// offer the card
		Offer offer = new Offer(OFFERING_USER, card, 6000, GeoSpatialUtils.createPoint(0.15D, 122.2D), Boolean.TRUE);
		offer.setAutoAccept(Boolean.TRUE);
		offerDao.save(offer);

		// ensure that it all ended up in the database
		assertEquals(1, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("Offer"));

		// now scan for the offer
		List<CandidateOffer> availableOffers = wooshController.findOffers(0.15D, 122.2D);
		
		assertEquals(1, super.countRowsInTable("Scan"));
//		assertEquals(1, super.countRowsInTable("Acceptance"));
		assertEquals(1, super.countRowsInTable("Card"));
		
		assertEquals(1, availableOffers.size());
		
	}
	
	@Test
	public void acceptingYourOwnOffersIsPrevented() {

		// create a card that can be offered
		Card card = new Card(END_USER /*, "Test" */);
		cardDao.save(card);

		// offer the card
		Offer offer = new Offer(END_USER, card, 6000, GeoSpatialUtils.createPoint(0.15D, 122.2D), Boolean.TRUE);
		offer.setAutoAccept(Boolean.TRUE);
		offerDao.save(offer);
		
		// ensure that it all ended up in the database
		assertEquals(1, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("Offer"));

		// now scan for the offer
		List<CandidateOffer> availableOffers = wooshController.findOffers(0.15D, 122.2D);
		
		assertEquals(1, super.countRowsInTable("Scan"));
		assertEquals(0, super.countRowsInTable("Acceptance"));
		assertEquals(1, super.countRowsInTable("Card"));
		
		assertEquals(0, availableOffers.size());
		
	}

	@Test
	public void alreadyAcceptedOffersAreNotPresentedAgain() {

		// create a card that can be offered
		Card card = new Card(OFFERING_USER /*, "Test" */);
		cardDao.save(card);

		// offer the card
		Offer offer = new Offer(OFFERING_USER, card, 6000, GeoSpatialUtils.createPoint(0.15D, 122.2D), Boolean.TRUE);
		offer.setAutoAccept(Boolean.TRUE);
		offerDao.save(offer);

		// accept the offer
		Acceptance acceptance = new Acceptance(END_USER, card, offer, true, new Timestamp(Calendar.getInstance().getTimeInMillis()));
		acceptanceDao.save(acceptance);

		// create another card that can be offered
		Card card2 = new Card(OFFERING_USER /*, "Test Two" */);
		cardDao.save(card2);

		// offer the card
		Offer offer2 = new Offer(OFFERING_USER, card2, 6000, GeoSpatialUtils.createPoint(0.15D, 122.2D), Boolean.TRUE);
		offer2.setAutoAccept(Boolean.TRUE);
		offerDao.save(offer2);

		assertEquals(2, super.countRowsInTable("Card"));
		assertEquals(2, super.countRowsInTable("Offer"));
		assertEquals(1, super.countRowsInTable("Acceptance"));
		
		// now scan for the offer
		List<CandidateOffer> availableOffers = wooshController.findOffers(0.15D, 122.2D);

		// ensure that only 1 offer is made (there are two offers in the database but one has already been accepted)
		assertEquals(1, availableOffers.size());

	}
	

}
