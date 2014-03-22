package com.luminos.woosh.dao.hibernate;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.controller.SyncController;
import com.luminos.woosh.dao.SynchronizableDao;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.ReadOnlySynchronizationEntity;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.UserScopedEntity;

/**
 * 
 * @author Ben
 */
@Repository
@Deprecated
public class SynchronizableDaoHibernateImpl extends GenericWooshDaoHibernateImpl<Synchronizable> implements SynchronizableDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Synchronizable> findAllAfter(Class<?> type, User user, Integer page, Timestamp lastUpdated) {
		Criteria criteria = super.getSession().createCriteria(type);
				
		criteria.add(Restrictions.gt("lastUpdated", lastUpdated))
				.add(RECORD_IS_NOT_DELETED)
				.setMaxResults(SyncController.DEFAULT_PAGE_SIZE)
				.setFirstResult(page * SyncController.DEFAULT_PAGE_SIZE)
				.addOrder(Order.asc("lastUpdated"));
		
		if ( UserScopedEntity.class.isAssignableFrom(type) ) {
			criteria.add(Restrictions.eq("owner", user));
		}
		
		return criteria.list();
	}

	@Override
	public Integer countPagesAfter(Class<?> type, User user, Timestamp lastUpdated) {
		Criteria criteria = super.getSession().createCriteria(type);
		
		criteria.add(Restrictions.gt("lastUpdated", lastUpdated))
				.add(RECORD_IS_NOT_DELETED)
				.setProjection(Projections.rowCount());						

		if ( UserScopedEntity.class.isAssignableFrom(type) ) {
			criteria.add(Restrictions.eq("owner", user));
		}

		return ((Integer) criteria.uniqueResult()) / SyncController.DEFAULT_PAGE_SIZE;
	}

	@Override
	public Object findByClientId(String clientId, Class<?> clazz) {
		return (Object) getSession().createCriteria(clazz)
											.add(Restrictions.eq("clientId", clientId))
											.uniqueResult();
	}

	@Override
	public void save(User user) {
		getSession().saveOrUpdate(user);
		getSession().flush();
	}

	@Override
	public void save(ReadOnlySynchronizationEntity entity) {
		getSession().saveOrUpdate(entity);
		getSession().flush();
	}

	@Override
	public void save(ReadOnlySynchronizationEntity... entities) {
		for (ReadOnlySynchronizationEntity entity : entities) {
			getSession().saveOrUpdate(entity);
			getSession().flush();
		}
	}

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
