package com.luminos.woosh.dao.hibernate;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
@Repository
public class CardDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Card> implements CardDao {

	public Card findByClientId(String id, User user) {
		return (Card) getSession().createCriteria(Card.class)
								  .createAlias("data", "d", CriteriaSpecification.LEFT_JOIN)
								  .setFetchMode("data", FetchMode.JOIN)
				   				  .add(Restrictions.eq("clientId", id))
				   				  .add(Restrictions.eq("owner", user))
				   				  .uniqueResult();		
	}
	
}
