package com.luminos.woosh.domain.processor;

import com.luminos.woosh.dao.SynchronizableDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.Processor;

/**
 * Whenever we see a new card posted we record it against the user that posted it.
 * 
 * @author Ben
 */
public class NewCardProcessor implements Processor<Card, Card> {

	@Override
	public Card process(User user, Card entity, SynchronizableDao repository) {
		user.addCard(entity);

		repository.save(entity);
		repository.save(user);
		
		return entity;
	}

}
