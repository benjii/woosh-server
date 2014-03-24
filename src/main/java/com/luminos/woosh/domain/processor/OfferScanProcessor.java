package com.luminos.woosh.domain.processor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.luminos.woosh.dao.SynchronizableDao;
import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.Processor;

/**
 * 
 * @author Ben
 */
@Deprecated
public class OfferScanProcessor implements Processor<Scan, List<Card>> {

	/**
	 * Performs a scan for a list of offers, from which a set of offer candidates are produced.
	 */
	@Override
	public List<Card> process(User user, Scan scan, SynchronizableDao repository) {
		
		// find candidate offers based upon the location of the scan
		List<Offer> offers = repository.findOffersWithinRange(scan);
		List<Card> cards = new ArrayList<Card>();
		
		// for each offer that we find create and cloned card and an acceptance
		for (Offer offer : offers) {

			// clone the card
			Card cardForOffer = offer.getCard().clone(user, offer);
			repository.save(cardForOffer);
			
			cards.add(cardForOffer);

			// create an acceptance entity (by default acceptance entities are not accepted)
			Acceptance acceptance = null;
			if (offer.getAutoAccept()) {
				acceptance = new Acceptance(user, cardForOffer, offer, Boolean.TRUE, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			} else {
				acceptance = new Acceptance(user, cardForOffer, offer);				
			}

			// save the acceptance
			repository.save(acceptance);

			// store the offer and acceptance entities on the scan
//			scan.addCard(cardForOffer);
			scan.addOffer(offer);
			
			// record all data against the user and flush to the database
			user.addScan(scan);
			user.addAcceptance(acceptance);
			user.addCard(cardForOffer);
		}

		// save the scan (so that the candidates relationships are recorded
		repository.save(scan);

		// save the user to the database - we may have recorded new information against the user
		repository.save(user);

		// pass the candidates back
		return cards;
	}

}
