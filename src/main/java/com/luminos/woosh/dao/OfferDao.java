package com.luminos.woosh.dao;

import java.util.List;

import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;

/**
 * 
 * @author Ben
 */
public interface OfferDao extends GenericLuminosDao<Offer> {

	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Offer> findOffersWithinRange(Scan scan);

}
