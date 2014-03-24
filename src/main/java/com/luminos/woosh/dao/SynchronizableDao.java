package com.luminos.woosh.dao;

import java.sql.Timestamp;
import java.util.List;

import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.ReadOnlySynchronizationEntity;
import com.luminos.woosh.synchronization.Synchronizable;

/**
 * 
 * @author Ben
 */
@Deprecated
public interface SynchronizableDao extends GenericWooshDao<Synchronizable> {

	/**
	 * This method retrieves all instances of the class 'type' with a last updated time after 'lastUpdated'.
	 * 
	 * @param type
	 * @param lastUpdated
	 * @return
	 */
	List<Synchronizable> findAllAfter(Class<?> type, User user, Integer page, Timestamp lastUpdated); 
	
	/**
	 * Counts the number of pages for this type that remain to the sync'd.
	 * 
	 * @param type
	 * @param user
	 * @param lastUpdated
	 * @return
	 */
	Integer countPagesAfter(Class<?> type, User user, Timestamp lastUpdated);
	
	/**
	 * Retrieves a synchronizable entity from the database given the ID and the class type. Hibernate requires
	 * the class type so that it knows what table to perform the query on.
	 * 
	 * @param clientId
	 * @param clazz
	 * @return
	 */
	Object findByClientId(String clientId, Class<?> clazz);

	/**
	 * 
	 * @param user
	 */
	void save(User user);

	/**
	 * 
	 * @param entity
	 */
	void save(ReadOnlySynchronizationEntity entity);

	/**
	 * 
	 * @param entity
	 */
	void save(ReadOnlySynchronizationEntity... entity);

	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Offer> findOffersWithinRange(Scan scan);

}
