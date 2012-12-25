package com.luminos.woosh.dao.hibernate;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
@Repository
public class AcceptanceDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Acceptance> implements AcceptanceDao {

	@Override
	public Acceptance findForOffer(Offer offer, User user) {
		return (Acceptance) getSession().createCriteria(Acceptance.class)
										.add(Restrictions.eq("offer", offer))
										.add(Restrictions.eq("owner", user))
										.uniqueResult();
	}

}
