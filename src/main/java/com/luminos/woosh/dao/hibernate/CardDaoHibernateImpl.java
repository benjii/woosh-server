package com.luminos.woosh.dao.hibernate;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
@Repository
public class CardDaoHibernateImpl extends GenericWooshDaoHibernateImpl<Card> implements CardDao {

	public Card findByClientId(String id, User user) {
		return (Card) getSession().createCriteria(Card.class)
								  .createAlias("data", "d", CriteriaSpecification.LEFT_JOIN)
								  .setFetchMode("data", FetchMode.JOIN)
				   				  .add(Restrictions.eq("clientId", id))
				   				  .add(Restrictions.eq("owner", user))
				   				  .uniqueResult();		
	}

	@SuppressWarnings("unchecked")
	public List<Card> findAllOrderedByOfferStart(User user) {
		return (List<Card>) getSession().createCriteria(Card.class, "c")
										.createAlias("offers", "o", CriteriaSpecification.LEFT_JOIN)
										.add(RECORD_IS_NOT_DELETED)
										.add(Restrictions.eq("owner", user))
										.addOrder(Order.desc("o.offerStart"))
										.setResultTransformer(new DistinctRootEntityResultTransformer())
										.list();
		
	}

}
