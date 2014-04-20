package com.luminos.woosh.dao;

import java.util.List;

import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;

/**
 * 
 * @author Ben
 */
public interface OfferDao extends GenericWooshDao<Offer> {

	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Offer> findOffersWithinRange(Scan scan);

	/**
	 * Counts the number of active (non-expired) offers in the system. Any offer that has an offer end in the future
	 * is considered active.
	 * 
	 * @return The count of active offers.
	 */
	Integer countAllActive();

}
