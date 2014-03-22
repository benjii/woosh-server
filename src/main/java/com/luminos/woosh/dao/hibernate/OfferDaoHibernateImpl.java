package com.luminos.woosh.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;

/**
 * 
 * @author Ben
 */
@Repository
public class OfferDaoHibernateImpl extends GenericWooshDaoHibernateImpl<Offer> implements OfferDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Offer> findOffersWithinRange(Scan scan) {
		Criteria criteria = super.getSession().createCriteria(Offer.class, "o");

		// TODO is this the most optimal way to do this? can we do it with a single SQL statement?
		
		// get any offers that;
		//	a) have remaining hops
		//  b) not already been accepted by the user
		//	c) are 'active' (the current time is within the offer period)
		//	d) the offer was not made by the user that is scanning
		//	e) the scan took place within the region of the offer

		// get the list of offers that have already been accepted by the user (we don't want to offer these again)
		Collection<Offer> offersAlreadyAcceptedByUser = super.getSession().createCriteria(Acceptance.class, "a")
																   		  .setProjection(Projections.distinct(Projections.property("a.offer")))
																   		  .add(Restrictions.eq("a.owner", scan.getOwner()))
																   		  .list();
		
		// find the full list of available offers, including the ones that have arleady been accepted (but not owned by the scanning user)
		criteria.add(RECORD_IS_NOT_DELETED)
				.add(Restrictions.ne("remainingHops", 0))
				.add(Restrictions.lt("offerStart", scan.getScannedAt()))
				.add(Restrictions.gt("offerEnd", scan.getScannedAt()))
				.add(Restrictions.ne("owner", scan.getOwner()))
				.add(Restrictions.sqlRestriction("st_distance_spheroid(this_.offerregion, st_geomfromtext('" + scan.getLocation().toText() +"'), 'SPHEROID[\"WGS 84\", 6378137, 298.257223563]') < 300"));
						
		List<Offer> availableOffers = (List<Offer>) criteria.list();
		
		// subtract the already accepted offers from the full list of offers
		Collection<Offer> offersToPresent = CollectionUtils.subtract(availableOffers, offersAlreadyAcceptedByUser);
		
		return new ArrayList<Offer>(offersToPresent);
	}

}
