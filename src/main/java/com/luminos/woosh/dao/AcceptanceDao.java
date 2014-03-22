package com.luminos.woosh.dao;

import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
public interface AcceptanceDao extends GenericWooshDao<Acceptance> {

	/**
	 * 
	 * @param offer
	 * @param user
	 * @return
	 */
	Acceptance findForOffer(Offer offer, User user);
	
}
