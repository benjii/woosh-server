package com.luminos.woosh.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 *
 * @param <T>
 */
public interface GenericWooshDao<T> {

	/**
	 * Saves or updates an entity.
	 * 
	 * @param entity
	 */
	void save(T entity);

	/**
	 * Saves up updates a set of entities (of the same type).
	 * 
	 * @param entity
	 */
	void save(T... entities);

	/**
	 * 
	 * @param entity
	 */
	void refresh(T entity);
	
	/**
	 * 
	 * @param entity
	 */
	void evict(T entity);

	/**
	 * 
	 * @param entity
	 */
	void flush();

	/**
	 * 
	 * @param entity
	 */
	void clear();

	/**
	 * The default implementation for finding an entity by UUID. Subclasses may override this for more detailed behaviour.
	 * 
	 * @param uuid
	 * @return
	 */
	T findById(String id);
	
	/**
	 * The default implementation for finding an entity by client UUID. 
	 * 
	 * @param id
	 * @return
	 */
	T findByClientId(String id);
		
	/**
	 * Returns all of the entities of a given type - use carefully.
	 * 
	 * @return
	 */
	List<T> findAll();

	/**
	 * 
	 * @param user
	 * @return
	 */
	List<T> findAll(User user);

	/**
	 * 
	 * @param user
	 * @param orderColumn
	 * @return
	 */
	List<T> findAll(User user, Order order);

	/**
	 * Returns the total number of non-deleted instances of an entity.
	 * 
	 * @return
	 */
	Integer count();

	/**
	 * Returns the total number of instances of an entity, including deleted ones.
	 * 
	 * @return
	 */
	Integer countAll();

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	Boolean exists(String id);

	/**
	 * 
	 * @param name
	 */
	void enableFilter(String name);
	
	/**
	 * 
	 * @param name
	 */
	void disableFilter(String name);

	/**
	 * 
	 * @return
	 */
	Session openSession();
	
}
