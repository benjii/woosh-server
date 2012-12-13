package com.luminos.woosh.dao;

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

}
