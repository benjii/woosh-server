package com.luminos.woosh.dao;

import java.util.List;

import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
public interface CardDao extends GenericLuminosDao<Card> {

	/**
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	Card findByClientId(String id, User user);

	/**
	 * 
	 * @param user
	 * @return
	 */
	List<Card> findAllByOfferStart(User user);

}
