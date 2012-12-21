package com.luminos.woosh.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;

/**
 * 
 * @author Ben
 */
@Repository
public class OfferDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Offer>implements OfferDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Offer> findOffersWithinRange(Scan scan) {
		Criteria criteria = super.getSession().createCriteria(Offer.class);		

		// get any offers that are;
		//	a) have remaining hops
		//	b) are 'active' (the current time is within the offer period)
		//	c) the offer was not made by the user that is scanning
		//	d) the scan took place within the region of the offer
		
		criteria.add(RECORD_IS_NOT_DELETED)
				.add(Restrictions.ne("remainingHops", 0))
				.add(Restrictions.lt("offerStart", scan.getScannedAt()))
				.add(Restrictions.gt("offerEnd", scan.getScannedAt()))
				.add(Restrictions.ne("owner", scan.getOwner()))
				.add(Restrictions.sqlRestriction("st_distance_spheroid(this_.offerregion, st_geomfromtext('" + scan.getLocation().toText() +"'), 'SPHEROID[\"WGS 84\", 6378137, 298.257223563]') < 300"));
						
		return (List<Offer>) criteria.list();
	}

}
